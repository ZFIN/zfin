package org.zfin.sequence.load;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.FileUtil;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static htsjdk.samtools.util.ftp.FTPClient.READ_TIMEOUT;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

@Log4j2
abstract public class EnsemblTranscriptBase {

    protected static final String baseUrl = "https://rest.ensembl.org";
    protected String cdnaFileName = "Danio_rerio.GRCz11.cdna.all.fa";
    protected String ncrnaFileName = "Danio_rerio.GRCz11.ncrna.fa";


    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    public static final String REPORT_HOME_DIRECTORY = "/home/ensembl/";
    protected List<EnsemblErrorRecord> errorRecords = new ArrayList<>();

    protected record TranscriptRecord(Marker marker, String ensdartID, RichSequence richSequence) {
    }

    public void init() throws IOException {
        downloadFile(cdnaFileName, "cdna");
        downloadFile(ncrnaFileName, "ncrna");
    }

    protected Map<String, List<RichSequence>> getAllGeneTranscriptsFromFile() {
        Map<String, List<RichSequence>> geneTranscriptMap = getGeneTranscriptMap(cdnaFileName);
        Map<String, List<RichSequence>> geneNcRNATranscriptMap = getGeneTranscriptMap(ncrnaFileName);
        geneTranscriptMap.putAll(geneNcRNATranscriptMap);
        return geneTranscriptMap;
    }

    protected List<RichSequence> getAllFastaRecordsFromFile() {
        List<RichSequence> cdnaRecords = getAllFastaRecords(cdnaFileName);
        List<RichSequence> ncRnaRecords = getAllFastaRecords(ncrnaFileName);
        cdnaRecords.addAll(ncRnaRecords);
        return cdnaRecords;
    }


    protected static void downloadFile(String fileName, String directory) {
        String zippedFileName = fileName + ".gz";
        File file = new File(zippedFileName);
        if (file.exists()) {
            return;
        }

        String fileURL = "https://ftp.ensembl.org/pub/current_fasta/danio_rerio/" + directory + "/" + zippedFileName;

        try {
            FileUtils.copyURLToFile(
                new URL(fileURL),
                new File(zippedFileName),
                60000,
                READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileUtil.gunzipFile(zippedFileName);
    }

    protected static Map<String, List<RichSequence>> getGeneTranscriptMap(String fileName) {
        try {
            List<RichSequence> transcriptList = getFastaIterator(fileName);
            return transcriptList.stream().collect(Collectors.groupingBy(EnsemblTranscriptBase::getGeneId));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static List<RichSequence> getAllFastaRecords(String fileName) {
        try {
            return getFastaIterator(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Could not read FASTA files");
    }

    public static String getGeneId(RichSequence sequence) {

        String line = sequence.getDescription();
        String pattern = "(.*)(gene:)(ENSDARG.*)( gene_biotype)(.*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);

        if (m.find()) {
            return m.group(3);
        }
        return "";
    }


    private static List<RichSequence> getFastaIterator(String fileName) throws FileNotFoundException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fileReader);
        RichSequenceIterator iterator;
        SymbolTokenization symbolTokenization = RichSequence.IOTools.getNucleotideParser();
        iterator = RichSequence.IOTools.readFasta(br, symbolTokenization, new SimpleNamespace(""));

        List<RichSequence> sequenceList = new ArrayList<>();
        while (iterator.hasNext()) {
            try {
                sequenceList.add(iterator.nextRichSequence());
            } catch (BioException e) {
                e.printStackTrace();
            }
        }
        return sequenceList;
    }

    public List<MarkerDBLink> getMarkerDbLinks() {
        return getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
    }

    public static String getString(RichSequence richSequence) {
        return getUnversionedAccession(richSequence.getAccession());
    }

    public static String getUnversionedAccession(String versionedAccession) {
        return versionedAccession.split("\\.")[0];
    }

    protected record EnsemblTranscript(String id, String name, String type) {
    }

    private record EnsemblErrorRecord(String ensdartID, String ensdartName, int ensdartLength, String zfinID, String zfinName, String zfinIDExisting, String zfinNameExisting) {
    }

    protected EnsemblLoadSummaryItemDTO getEnsemblLoadSummaryItemDTO(Map<String, List<RichSequence>> geneTranscriptMap) {
        EnsemblLoadSummaryItemDTO dto = new EnsemblLoadSummaryItemDTO();
        dto.getCounts().put("ensemblGeneCount", (long) geneTranscriptMap.size());
        dto.getCounts().put("zfinEnsemblGeneCount", (long) getMarkerDbLinks().size());
        Set<RichSequence> transcriptSet = new HashSet<>(geneTranscriptMap.values().stream().flatMap(Collection::stream).toList());
        dto.getCounts().put("ensemblTranscriptCount", (long) transcriptSet.size());
        return dto;
    }

    private String actionsToJson(LoadActionsContainer actions) throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(actions);
    }

    protected void writeOutputReportFile(Set<LoadAction> actions, EnsemblLoadSummaryItemDTO summary) {
        String reportFile = "ensembl-transcript-load-report.html";

        log.info("Creating report file: " + reportFile);
        try {
            LoadActionsContainer actionsContainer = LoadActionsContainer.builder()
                .actions(actions)
                .summary(summary)
                .build();
            String jsonContents = actionsToJson(actionsContainer);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + REPORT_HOME_DIRECTORY + "/ensembl-transcript-report-template.html";
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            FileUtils.writeStringToFile(new File(reportFile), filledTemplate, "UTF-8");
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }


}

