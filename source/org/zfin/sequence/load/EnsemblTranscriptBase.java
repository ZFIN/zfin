package org.zfin.sequence.load;

import lombok.extern.log4j.Log4j2;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.marker.Marker;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.FileUtil;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

@Log4j2
abstract public class EnsemblTranscriptBase {

    protected static final String baseUrl = "https://rest.ensembl.org";
    public static final String CDNA_FILE_NAME = "Danio_rerio.GRCz11.cdna.all.fa";
    public static final String NCRNA_FILE_NAME = "Danio_rerio.GRCz11.ncrna.fa";
    public static final String ALL_FILE_NAME = "Danio_rerio.GRCz11.all.fa";


    protected record TranscriptRecord(Marker marker, String ensdartID, RichSequence richSequence) {
    }

    protected Map<String, List<RichSequence>> geneTranscriptMap;

    Set<LoadAction> actions = new HashSet<>();
    EnsemblLoadSummaryItemDTO dto;

    public void init() throws IOException {
        downloadFile(CDNA_FILE_NAME, "cdna");
        downloadFile(NCRNA_FILE_NAME, "ncrna");
        // <ensdargID, List<RichSequence>>
        geneTranscriptMap = getAllGeneTranscriptsFromFile();
    }

    public void initCondensed(File file) {
        // <ensdargID, List<RichSequence>>
        geneTranscriptMap = getGeneTranscriptMap(file.getAbsolutePath());
        System.out.println("Total Number of Ensembl Transcripts: " + geneTranscriptMap.size());
    }

    protected Map<String, List<RichSequence>> getAllGeneTranscriptsFromFile() {
        Map<String, List<RichSequence>> geneTranscriptMap = getGeneTranscriptMap(CDNA_FILE_NAME);
        Map<String, List<RichSequence>> geneNcRNATranscriptMap = getGeneTranscriptMap(NCRNA_FILE_NAME);
        geneTranscriptMap.putAll(geneNcRNATranscriptMap);
        return geneTranscriptMap;
    }

    protected List<RichSequence> getAllFastaRecordsFromFile() {
        List<RichSequence> cdnaRecords = getAllFastaRecords(CDNA_FILE_NAME);
        List<RichSequence> ncRnaRecords = getAllFastaRecords(NCRNA_FILE_NAME);
        cdnaRecords.addAll(ncRnaRecords);
        return cdnaRecords;
    }


    /**
     * Fetch one of the Ensembl fasta dumps, reusing the local copy only while it still matches
     * what the server would send.
     * <p>
     * The url goes through current_fasta, a moving pointer to the newest release, and the file
     * name stays the same from release to release for as long as the assembly stays GRCz11. The
     * old "skip if the file exists" check therefore pinned the load to whichever release happened
     * to be downloaded first, with nothing in the name to show it had gone stale.
     */
    protected static void downloadFile(String fileName, String directory) {
        String zippedFileName = fileName + ".gz";
        File zippedFile = new File(zippedFileName);

        String fileURL = "https://ftp.ensembl.org/pub/current_fasta/danio_rerio/" + directory + "/" + zippedFileName;

        try {
            new DownloadService().downloadFileIfChanged(zippedFile, new URL(fileURL));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Gunzip only when the decompressed copy is not the one inside this archive. The archive
        // carries the server's Last-Modified, and we copy that onto the decompressed file, so
        // equal timestamps mean the two are in sync. Comparing "older than" instead would break:
        // a refreshed archive is back-dated to the server's timestamp and can easily be older
        // than a fasta left over from the previous release.
        File decompressedFile = new File(fileName);
        if (!decompressedFile.exists() || decompressedFile.lastModified() != zippedFile.lastModified()) {
            FileUtil.gunzipFile(zippedFileName);
            decompressedFile.setLastModified(zippedFile.lastModified());
        }
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

    public static String getGeneIdFromZfinDefline(RichSequence sequence) {
        String line = sequence.getAccession();
        String[] token = line.split("\\|");
        return token[1];
    }


    private static List<RichSequence> getFastaIterator(String fileName) throws FileNotFoundException {
        System.out.println(fileName);
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

    protected EnsemblLoadSummaryItemDTO getEnsemblLoadSummaryItemDTO() {
        EnsemblLoadSummaryItemDTO dto = new EnsemblLoadSummaryItemDTO();
        dto.getCounts().put("ensemblGeneCount", (long) geneTranscriptMap.size());
        dto.getCounts().put("zfinEnsemblGeneCount", (long) getMarkerDbLinks().size());
        Set<RichSequence> transcriptSet = new HashSet<>(geneTranscriptMap.values().stream().flatMap(Collection::stream).toList());
        dto.getCounts().put("ensemblTranscriptCount", (long) transcriptSet.size());
        return dto;
    }

    protected void writeOutputReportFile() {
        File reportFile = new File("ensembl-transcript-load-report.html");
        log.info("Creating report file: " + reportFile);
        try {
            LoadActionsContainer container = LoadActionsContainer.builder()
                .actions(actions)
                .summary(dto)
                .build();
            new LoadActionReportAdapter().writeHtmlReport("Ensembl Transcript Load", container, reportFile);
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ")\n" + e.getMessage(), e);
        }
    }

    protected static File getCombinedFastaFile() {
        // validate the two inputs before deciding whether the combined file is still good: it is
        // derived from them, so "it exists" said nothing about whether it was built from the
        // release we are about to load against
        downloadFile(CDNA_FILE_NAME, "cdna");
        downloadFile(NCRNA_FILE_NAME, "ncrna");

        File allFile = new File(ALL_FILE_NAME);
        long newestInput = Math.max(new File(CDNA_FILE_NAME).lastModified(), new File(NCRNA_FILE_NAME).lastModified());
        if (allFile.exists() && allFile.lastModified() == newestInput) {
            return allFile;
        }
        try {
            PrintWriter pw = new PrintWriter(ALL_FILE_NAME);
            BufferedReader br = new BufferedReader(new FileReader(CDNA_FILE_NAME));
            String line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }
            br = new BufferedReader(new FileReader(NCRNA_FILE_NAME));
            line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }
            pw.flush();
            br.close();
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        allFile = new File(ALL_FILE_NAME);
        // mark the combined file as built from these inputs, so the check above can tell next time
        allFile.setLastModified(newestInput);
        return allFile;
    }


}

