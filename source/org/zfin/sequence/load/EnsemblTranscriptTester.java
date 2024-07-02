package org.zfin.sequence.load;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.Species;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.services.VocabularyService;
import org.zfin.marker.*;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.zfin.framework.services.VocabularyEnum.TRANSCRIPT_ANNOTATION_METHOD;
import static org.zfin.marker.Marker.Type.TSCRIPT;
import static org.zfin.marker.TranscriptType.Type.MRNA;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE;

public class EnsemblTranscriptTester {

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptTester loader = new EnsemblTranscriptTester();
        loader.init();
    }

    Map<Marker, List<TranscriptDBLink>> geneEnsdartMap;
    Map<String, MarkerDBLink> ensdargMap;


    public void init() throws IOException {
        ensdargMap = getMarkerDBLinksWithVegaGenbankEnsemblAccessions();
        ensdargMap.putAll(getMarkerDBLinksWithGenbankEnsemblOnlyAccessions());
        geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();

        String sql = "select * from ensembl_transcript_renaming";
        List<Object[]> results = HibernateUtil.currentSession().createNativeQuery(sql).getResultList();
        Map<String, Object[]> resultMap = results.stream().collect(Collectors.toMap(o -> (String) o[1], Function.identity()));
        Map<String, String> allEnsdartIdsWithNewName = resultMap.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> (String) entry.getValue()[2], (o1, o2) -> o1, HashMap::new));

        getSequenceRepository().getAllRelevantEnsemblTranscripts().forEach((marker, transcriptDBLinks) -> {
            transcriptDBLinks.stream().filter(transcriptDBLink -> allEnsdartIdsWithNewName.containsKey(transcriptDBLink.getAccessionNumber()))
                .toList().forEach(transcriptDBLink -> {
                    if (!transcriptDBLink.getTranscript().getAbbreviation().equals(resultMap.get(transcriptDBLink.getAccessionNumber())[2])) {
                        System.out.println(transcriptDBLink.getAccessionNumber() + ":" + resultMap.get(transcriptDBLink.getAccessionNumber())[2]);
                    }
                });
        });
        System.exit(0);
    }

    private List<RichSequence> sequenceListToBeGenerated = new ArrayList<>();

    private final MarkerType transcriptMarkerType = getMarkerRepository().getMarkerTypeByName(TSCRIPT.name());
    private final TranscriptType transcriptTypeForName = getMarkerRepository().getTranscriptTypeForName(MRNA.toString());
    private final MarkerRelationshipType markerRelationshipType = getMarkerRepository().getMarkerRelationshipType(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT.toString());
    private final VocabularyService vocabularyService = new VocabularyService();
    private final ReferenceDatabase database = getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.ENSEMBL_TRANS, ForeignDBDataType.DataType.RNA, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
    private final DisplayGroup nucleotideSequ = getSequenceRepository().getDisplayGroup(DISPLAYED_NUCLEOTIDE_SEQUENCE);
    private final VocabularyTerm annotationMethod = vocabularyService.getVocabularyTerm(TRANSCRIPT_ANNOTATION_METHOD, "Ensembl");
    // Genotype hard-coded to TU
    private final Genotype tu = getExpressionRepository().getGenotypeByID("ZDB-GENO-990623-3");
    private final Publication pub = getPublicationRepository().getPublication("ZDB-PUB-240305-9");
    private final Person person = getProfileRepository().getPerson("ZDB-PERS-060413-1");

    private List<EnsemblErrorRecord> errorRecords = new ArrayList<>();

    private record TranscriptRecord(Marker marker, String ensdartID, RichSequence richSequence) {
    }

    private void createReportNoEnsembl(Map<String, MarkerDBLink> genbankMap, String fileName) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));

        List<String> headerNames = List.of(
            "GeneID",
            "Genbbank ID"
        );
        CSVPrinter csvPrinterImportant = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader(headerNames.toArray(String[]::new)));

        genbankMap.forEach((genbankID, markerDBLink) -> {
            List<String> values = new ArrayList<>(headerNames.size());
            values.add(markerDBLink.getMarker().getZdbID());
            values.add(genbankID);

            try {
                String[] values1 = values.toArray(String[]::new);
                csvPrinterImportant.printRecord((Object[]) values1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, MarkerDBLink> getMarkerDBLinksWithVegaGenbankEnsemblAccessions() {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        System.out.println("Total Number of Ensembl Genes In ZFIN: " + ensdargList.size());

        // vega gene list
        List<String> vegaGeneList = vegaList.stream().map(LinkDisplay::getAssociatedGeneID).toList();
        // genbank gene list
        List<String> genbankGeneList = genbankList.stream().map(markerDBLink1 -> markerDBLink1.getMarker().getZdbID()).toList();
        ensdargList.removeIf(markerDBLink -> !vegaGeneList.contains(markerDBLink.getMarker().getZdbID()));
        //System.out.println("Number of Ensembl Genes that also have a Vega Gene: " + ensdargList.size());
        ensdargList.removeIf(markerDBLink -> !genbankGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that also have a Vega and GenBank Gene: " + ensdargList.size());
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));

        return ensdargMap;
    }


    private Map<String, MarkerDBLink> getMarkerDBLinksWithGenbankEnsemblOnlyAccessions() {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        // vega gene list
        List<String> vegaGeneList = vegaList.stream().map(LinkDisplay::getAssociatedGeneID).toList();
        // genbank gene list
        List<String> genbankGeneList = genbankList.stream().map(markerDBLink1 -> markerDBLink1.getMarker().getZdbID()).toList();
        ensdargList.removeIf(markerDBLink -> !genbankGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that also have a Vega Gene: " + ensdargList.size());
        ensdargList.removeIf(markerDBLink -> vegaGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that have a Genbank and no Vega Gene: " + ensdargList.size());
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));
        try {
            createReportNoEnsembl(ensdargMap, "report-transcript-ensembl-genbank-no-vega");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ensdargMap;
    }

    Map<String, EnsemblTranscript> ensemblTranscriptMap = new HashMap<>();

    private void extracted() {
        try {
            List<EnsemblTranscript> list = parseEnsemblBioMartFile(new File("transcript-name-ensembl.txt"));
            ensemblTranscriptMap = list.stream().collect(Collectors.toMap(EnsemblTranscript::id, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<EnsemblTranscript> parseEnsemblBioMartFile(File downloadedFile) throws Exception {
        List<EnsemblTranscript> transcripts = new ArrayList<>();
        Reader in = new FileReader(downloadedFile);
        Iterable<CSVRecord> records = CSVFormat.TDF
            .withHeader(headers)
            .withSkipHeaderRecord()
            .parse(in);
        for (CSVRecord record : records) {
            EnsemblTranscript tscript = new EnsemblTranscript(record.get(0), record.get(1), record.get(2));
            transcripts.add(tscript);
        }
        System.out.println("Found " + transcripts.size() + " transcript records in BioMart export file for finding the transcript name by ensdart ID");
        return transcripts;
    }

    private static final String[] headers = {"Transcript stable ID", "Transcript name", "Transcript typ"};

    private record EnsemblTranscript(String id, String name, String type) {
    }

    private record EnsemblErrorRecord(String ensdartID, String ensdartName, int ensdartLength, String zfinID, String zfinName, String zfinIDExisting, String zfinNameExisting) {
    }
}

