package org.zfin.sequence.load;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.zfin.Species;
import org.zfin.alliancegenome.JacksonObjectMapperFactoryZFIN;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.services.VocabularyService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.*;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.marker.presentation.MarkerRelationshipFormBean;
import org.zfin.marker.presentation.RelatedTranscriptDisplay;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;
import org.zfin.sequence.service.TranscriptService;
import org.zfin.util.FileUtil;
import si.mazi.rescu.ClientConfig;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static htsjdk.samtools.util.ftp.FTPClient.READ_TIMEOUT;
import static java.util.stream.Collectors.joining;
import static org.zfin.framework.services.VocabularyEnum.TRANSCRIPT_ANNOTATION_METHOD;
import static org.zfin.marker.Marker.Type.TSCRIPT;
import static org.zfin.marker.TranscriptType.Type.MRNA;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE;

/**
 * This class runs system exec calls robustly.
 */
public class EnsemblTranscriptFastaReadProcess {

    private static final String baseUrl = "https://rest.ensembl.org";
    private static final ClientConfig config = new ClientConfig();

    static {
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
    }

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptFastaReadProcess loader = new EnsemblTranscriptFastaReadProcess();
        loader.init();
    }

    private Map<String, List<RichSequence>> allEnsemblProvidedGeneMap;

    public void init() throws IOException {
        loadSequenceMapFromDownloadFile();

        // <ensdargID, DBLink>
        Map<String, MarkerDBLink> ensdargMap = getMarkerDBLinks();
        Map<Marker, List<TranscriptDBLink>> geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        System.out.println("Total Number of Genes with Ensembl Transcripts In ZFIN: " + geneEnsdartMap.size());

        List<Marker> ensemblGenesToBeImported = getEnsemblAccessionsToBeImported(geneEnsdartMap);
        createTranscriptRecords(ensdargMap, geneEnsdartMap, ensemblGenesToBeImported);
        System.exit(0);

        createReportFiles(geneEnsdartMap);


        allEnsemblProvidedGeneMap.entrySet().removeIf(entry -> !ensdargMap.containsKey(entry.getKey()));
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file matching a gene record in ZFIN: " + allEnsemblProvidedGeneMap.size());

        // remove transcripts that are being found in ZFIN
/*
        sortedGeneTranscriptMapCleaned.entrySet().forEach(entry -> {
            entry.getValue().removeIf(richSequence -> ensdartListString.contains(getGeneIdFromVersionedAccession(richSequence.getAccession())));
        });
*/
        // remove accession without any new transcripts left
        allEnsemblProvidedGeneMap.entrySet().removeIf(entry -> entry.getValue().size() == 0);
        System.out.println("Total Number of Ensembl Genes with new transcripts in FASTA file matching a gene record in ZFIN: " + allEnsemblProvidedGeneMap.size());
        System.out.println("Total Number of Ensembl Transcripts missing in ZFIN: " + allEnsemblProvidedGeneMap.values().stream().map(List::size).reduce(0, (integer, richSequences) -> integer + richSequences));

        allEnsemblProvidedGeneMap.forEach((s, richSequences) -> {
                richSequences.forEach(richSequence -> {
                    System.out.println(ensdargMap.get(s).getMarker().getZdbID() + "," + getGeneIdFromVersionedAccession(richSequence.getAccession()));
                });
            }
        );
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

    private void createSingleTranscript(Marker marker, String ensdartID, RichSequence ensemblSequence) {
/*
        EnsemblTranscriptRESTInterface api = RestProxyFactory.createProxy(EnsemblTranscriptRESTInterface.class, baseUrl, config);
        EnsemblTranscript ensemblTranscript = api.getTranscriptInfo(ensdartID, "application/json");
*/

        String transcriptName = getTranscriptName(ensdartID).toLowerCase();
        Marker existingMarker = getMarkerRepository().getMarkerByAbbreviation(transcriptName);
        if (existingMarker != null) {
            EnsemblErrorRecord errorRecord = new EnsemblErrorRecord(ensdartID,
                transcriptName,
                ensemblSequence.length(),
                marker.getZdbID(),
                marker.getAbbreviation(),
                existingMarker.getZdbID(),
                existingMarker.getAbbreviation());
            errorRecords.add(errorRecord);
        }
        String bioType = getTranscriptType(ensdartID);
        Transcript transcript = new Transcript();
        transcript.setEnsdartId(ensdartID);
        transcript.setOwner(person);
        transcript.setAbbreviation(transcriptName);
        transcript.setName(transcriptName);
        transcript.setMarkerType(transcriptMarkerType);
        // if biotype = protein_coding => mRNA
        // otherwise exception
        if (!bioType.equals("protein_coding")) {
            if (bioType.equals("retained_intron") || bioType.equals("processed_transcript") || bioType.equals("nonsense_mediated_decay")) {
                return;
            }
            throw new RuntimeException("Could not map biotype " + bioType + " for transcript " + transcriptName + " with accession " + ensdartID);
        }
        transcript.setTranscriptType(transcriptTypeForName);

        HibernateUtil.createTransaction();
        try {
            MarkerRelationship relationship = new MarkerRelationship();
            relationship.setFirstMarker(marker);
            relationship.setSecondMarker(transcript);
            relationship.setType(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
            MarkerRelationshipFormBean markerRelationshipFormBean = null;
            relationship.setMarkerRelationshipType(markerRelationshipType);
            marker.getFirstMarkerRelationships().add(relationship);

            TranscriptDBLink transcriptDBLink = new TranscriptDBLink();
            transcriptDBLink.setTranscript(transcript);
            transcriptDBLink.setAccessionNumber(ensdartID);
            transcriptDBLink.setLength(ensemblSequence.length());
            database.getDisplayGroups().add(nucleotideSequ);
            transcriptDBLink.setReferenceDatabase(database);

            transcript.setStrain(tu);
            transcript.setAnnotationMethod(annotationMethod);
            HibernateUtil.currentSession().save(transcript);
            HibernateUtil.currentSession().save(relationship);
            HibernateUtil.currentSession().save(transcriptDBLink);
            PublicationAttribution attribution = getInfrastructureRepository().insertStandardPubAttribution(transcript.zdbID, pub);
            // attribute markerRelationship
            relationship.setPublications(Set.of(attribution));

            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            System.out.println("Failed to save transcript: " + ensdartID + " [" + transcript.getAbbreviation() + "]");
            HibernateUtil.rollbackTransaction();
            return;
        }
        //System.out.println(transcript.getZdbID());

        // create the fasta file
        ensemblSequence.setDescription(transcript.getZdbID() + " " + ensemblSequence.getDescription());
        sequenceListToBeGenerated.add(ensemblSequence);
    }

    private void createTranscriptRecords(Map<String, MarkerDBLink> ensdargMap,
                                         Map<Marker, List<TranscriptDBLink>> geneEnsdartMap,
                                         List<Marker> genesToAddTranscripts) {
        System.out.println("Number of Genes for which transcripts need to be added: " + genesToAddTranscripts.size());
        AtomicInteger index = new AtomicInteger(0);
        System.out.println();
        genesToAddTranscripts.forEach(marker -> {
            // get the ENSDARG ID for the given gene
            List<Map.Entry<String, MarkerDBLink>> entries = ensdargMap.entrySet().stream().filter(entry -> entry.getValue().getMarker().getZdbID().equals(marker.getZdbID())).toList();
            if (CollectionUtils.isEmpty(entries))
                return;
            MarkerDBLink link = entries.get(0).getValue();
            // obtain all transcripts from Ensembl for the given ENSDARG ID
            List<RichSequence> transcriptsEnsembl = allEnsemblProvidedGeneMap.get(link.getAccessionNumber());
            if (transcriptsEnsembl == null) {
                return;
            }
            transcriptsEnsembl.forEach(richSequence -> {
                String ensdartID = getString(richSequence);
/*
                List<Transcript> transcriptsInZFIN = TranscriptService.getRelatedTranscripts(marker).stream()
                    .map(relatedMarker -> TranscriptService.convertMarkerToTranscript(relatedMarker.getMarker())).toList();
                List<String> existingEnsdartIDs = transcriptsInZFIN.stream().map(Transcript::getEnsdartId).toList();
*/
                // only create those ensembl transcripts that do not exist yet in ZFIN
                if (geneEnsdartMap.get(marker).stream().map(DBLink::getAccessionNumber).toList().contains(ensdartID)) {
                    return;
                }
                createSingleTranscript(marker, ensdartID, richSequence);
            });
            if (index.incrementAndGet() % 10 == 0) {
                System.out.print(index + "..");
            }
        });

        sequenceListToBeGenerated.forEach(richSequence -> {
            RichSequence.IOTools.SingleRichSeqIterator iterator = new RichSequence.IOTools.SingleRichSeqIterator(richSequence);
            try {
                write("ensembl-transcripts.1.fasta", iterator);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        errorRecords.forEach(System.out::println);

        List<String> headerNames = List.of(
            "Ensembl ID",
            "Ensembl Name",
            "Length of transcripts",
            "Associated ZFIn Gene ID",
            "Associated ZFIn Gene symbol",
            "Existing ZFIN  Transcript ID",
            "Existing ZFIN  Transcript symbol"
        );

        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(Paths.get("ensembl-error-report.txt"));
            CSVPrinter csvPrinterImportant = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader(headerNames.toArray(String[]::new)));
            errorRecords.forEach(record -> {
                List<String> vals = new ArrayList<>();
                vals.add(record.ensdartID);
                vals.add(record.ensdartName);
                vals.add(record.ensdartLength+"");
                vals.add(record.zfinID);
                vals.add(record.zfinName);
                vals.add(record.zfinIDExisting);
                Object[] values = vals.toArray();

                try {
                    csvPrinterImportant.printRecord(values);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private List<Marker> getEnsemblAccessionsToBeImported(Map<Marker, List<TranscriptDBLink>> geneEnsdartMap) {
        List<Marker> listOfGeneInfoToBeImported = new ArrayList<>();

        geneEnsdartMap.forEach((gene, markerDBLink) -> {
            List<LinkDisplay> list = getMarkerRepository().getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE);
            List<LinkDisplay> ensdarg = list.stream().filter(linkDisplay -> linkDisplay.getAccession().startsWith("ENSDARG")).toList();
            if (CollectionUtils.isEmpty(ensdarg))
                return;
            String ensdargAccession = ensdarg.get(0).getAccession();
            List<RichSequence> transcriptsEnsembl = allEnsemblProvidedGeneMap.get(ensdargAccession);
            if (transcriptsEnsembl == null) {
                return;
            }
            Set<String> zfinAccessions = markerDBLink.stream().map(TranscriptDBLink::getAccessionNumber).collect(Collectors.toSet());
            Set<String> ensemblAccessions = transcriptsEnsembl.stream().map(EnsemblTranscriptFastaReadProcess::getString).collect(Collectors.toSet());
            if (!zfinAccessions.equals(ensemblAccessions)) {
                RelatedTranscriptDisplay disp = TranscriptService.getRelatedTranscriptsForGene(gene);
                List<Integer> lengthsZfin = disp.getTranscripts().stream().map(relatedMarker -> relatedMarker.getDisplayedSequenceDBLinks().stream().map(DBLink::getLength).collect(Collectors.toList())).flatMap(Collection::stream).sorted().toList();
                List<Integer> lengthsEnsembl = transcriptsEnsembl.stream().map(s -> s.getInternalSymbolList().length()).sorted().toList();
                if (lengthsEnsembl.get(lengthsEnsembl.size() - 1) > lengthsZfin.get(lengthsZfin.size() - 1)) {
                    listOfGeneInfoToBeImported.add(gene);
                }
            }
        });
        return listOfGeneInfoToBeImported;
    }

    private void createReportFiles(Map<Marker, List<TranscriptDBLink>> geneEnsdartMap) throws IOException {
        List<String> listOfGeneIdsWithNoDifference = new ArrayList<>();
        List<String> listOfGeneIdsWithDifferencesEnsemblLongest = new ArrayList<>();
        List<String> listOfGeneIdsWithDifferences = new ArrayList<>();
        List<String> listOfGeneIdsNotFoundInFile = new ArrayList<>();
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("report-transcript-ensembl-important.txt"));
        BufferedWriter ensemblWriter = Files.newBufferedWriter(Paths.get("report-transcript-ensembl.txt"));

        List<String> headerNames = List.of(
            "GeneID",
            "Number of Transcripts ZFIN",
            "Lengths of transcripts ZFIN",
            "Length of Transcripts not found in ensembl",
            "Ensembl ID",
            "Number of Transcripts Ensembl",
            "Lengths of transcripts Ensembl",
            "Length of Transcripts not found in ZFIN"
        );
        CSVPrinter csvPrinterImportant = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader(headerNames.toArray(String[]::new)));
        CSVPrinter csvPrinter = new CSVPrinter(ensemblWriter, CSVFormat.DEFAULT
            .withHeader(headerNames.toArray(String[]::new)));

        geneEnsdartMap.forEach((gene, markerDBLink) -> {
            List<String> values = new ArrayList<>(headerNames.size());

            List<LinkDisplay> list = getMarkerRepository().getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE);
            List<LinkDisplay> ensdarg = list.stream().filter(linkDisplay -> linkDisplay.getAccession().startsWith("ENSDARG")).toList();
            if (CollectionUtils.isEmpty(ensdarg))
                return;
            String ensdargAccession = ensdarg.get(0).getAccession();
            List<RichSequence> transcriptsEnsembl = allEnsemblProvidedGeneMap.get(ensdargAccession);
            if (transcriptsEnsembl == null) {
                listOfGeneIdsNotFoundInFile.add(ensdargAccession);
                return;
            }
            Set<String> zfinAccessions = markerDBLink.stream().map(TranscriptDBLink::getAccessionNumber).collect(Collectors.toSet());
            Set<String> ensemblAccessions = transcriptsEnsembl.stream().map(richSequence -> getString(richSequence)).collect(Collectors.toSet());
            if (zfinAccessions.equals(ensemblAccessions)) {
                listOfGeneIdsWithNoDifference.add(gene.getZdbID());
            } else {
                RelatedTranscriptDisplay disp = TranscriptService.getRelatedTranscriptsForGene(gene);
                List<Integer> lengthsZfin = disp.getTranscripts().stream().map(relatedMarker -> relatedMarker.getDisplayedSequenceDBLinks().stream().map(DBLink::getLength).collect(Collectors.toList())).flatMap(Collection::stream).sorted().toList();
                List<Integer> lengthsEnsembl = transcriptsEnsembl.stream().map(s -> s.getInternalSymbolList().length()).sorted().toList();
                if (lengthsEnsembl.get(lengthsEnsembl.size() - 1) > lengthsZfin.get(lengthsZfin.size() - 1)) {
                    listOfGeneIdsWithDifferencesEnsemblLongest.add(gene.getZdbID());
                } else {
                    listOfGeneIdsWithDifferences.add(gene.getZdbID());
                }

                values.add(gene.getZdbID());
                values.add(String.valueOf(zfinAccessions.size()));
                values.add(disp.getTranscripts().stream().map(relatedMarker -> relatedMarker.getDisplayedSequenceDBLinks().stream().map(link -> String.valueOf(link.getLength())).collect(joining(",")))
                    .collect(joining("|")));
                values.add(CollectionUtils.subtract(lengthsZfin, lengthsEnsembl) + "");
                values.add(ensdargAccession);
                values.add(ensemblAccessions.size() + "");
                values.add(transcriptsEnsembl.stream().map(s -> String.valueOf(s.getInternalSymbolList().length())).collect(Collectors.joining("|")));
                values.add(CollectionUtils.subtract(lengthsEnsembl, lengthsZfin) + "");

                try {
                    String[] values1 = values.toArray(String[]::new);
                    if (lengthsEnsembl.get(lengthsEnsembl.size() - 1) > lengthsZfin.get(lengthsZfin.size() - 1)) {
                        csvPrinterImportant.printRecord((Object[]) values1);
                    } else {
                        csvPrinter.printRecord((Object[]) values1);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            writer.close();
            ensemblWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getString(RichSequence richSequence) {
        return richSequence.getAccession().split("\\.")[0];
    }

    private static Map<String, MarkerDBLink> getMarkerDBLinks() {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes();
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        System.out.println("Total Number of Ensembl Genes In ZFIN: " + ensdargList.size());
        // vega gene list
        List<String> vegaGeneList = vegaList.stream().map(LinkDisplay::getAssociatedGeneID).toList();
        // genbank gene list
        List<String> genbankGeneList = genbankList.stream().map(markerDBLink1 -> markerDBLink1.getMarker().getZdbID()).toList();
        ensdargList.removeIf(markerDBLink -> !vegaGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that also have a Vega Gene: " + ensdargList.size());
        ensdargList.removeIf(markerDBLink -> !genbankGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that also have a Vega and GenBank Gene: " + ensdargList.size());
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));

        return ensdargMap;
    }

    private void loadSequenceMapFromDownloadFile() {
        String fileName = "Danio_rerio.GRCz11.cdna.all.fa";
        downloadFile(fileName);

        // <ensdargID, List<RichSequence>>
        Map<String, List<RichSequence>> geneTranscriptMap = getGeneTranscriptMap(fileName);
        Map<String, List<RichSequence>> sortedGeneTranscriptMap = geneTranscriptMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        // remove version number from accession ID
        allEnsemblProvidedGeneMap = new LinkedHashMap<>();
        sortedGeneTranscriptMap.forEach((s, richSequences) -> {
            String cleanedID = s.split("\\.")[0];
            allEnsemblProvidedGeneMap.put(cleanedID, richSequences);
        });
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file: " + sortedGeneTranscriptMap.size());
    }

    private static void downloadFile(String fileName) {
        fileName = fileName + ".gz";
        String fileURL = "https://ftp.ensembl.org/pub/release-111/fasta/danio_rerio/cdna/" + fileName;

        try {
            FileUtils.copyURLToFile(
                new URL(fileURL),
                new File(fileName),
                60000,
                READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileUtil.gunzipFile(fileName);
    }

    private static void printFirstTerms(Map<String, List<RichSequence>> sortedMap, int limit) {
        sortedMap.entrySet().stream()
            .skip(0)
            .limit(limit)
            .forEach(entry -> {
                System.out.println(entry.getKey() + "\t" + entry.getValue().size());
            });
    }

    private static Map<String, List<RichSequence>> getGeneTranscriptMap(String fileName) {
        try {
            List<RichSequence> transcriptList = getFastaIterator(fileName);
            return transcriptList.stream().collect(Collectors.groupingBy(EnsemblTranscriptFastaReadProcess::getGeneId));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getGeneId(RichSequence sequence) {

        String line = sequence.getDescription();
        String pattern = "(.*)(gene:)(ENSDARG.*)( gene_biotype)(.*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);

        if (m.find()) {
            return m.group(3);
        }
        return null;
    }

    private static String getGeneIdFromVersionedAccession(String accession) {
        return accession.split("\\.")[0];
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

    private static void write(String fileName, SequenceIterator sequenceIterator) throws IOException {
        OutputStream outputStream = new FileOutputStream(fileName);
        Namespace namespace = new SimpleNamespace("ZFIN-Ensembl-transcripts");
        RichSequence.IOTools.writeFasta(outputStream, sequenceIterator, namespace);
    }

    Map<String, EnsemblTranscript> ensemblTranscriptMap = new HashMap<>();

    private String getTranscriptName(String accession) {
        if (ensemblTranscriptMap.size() > 0) {
            return ensemblTranscriptMap.get(accession).name();
        }
        extracted();
        return ensemblTranscriptMap.get(accession).name();
    }

    private void extracted() {
        try {
            List<EnsemblTranscript> list = parseEnsemblBioMartFile(new File("transcript-name-ensembl.txt"));
            ensemblTranscriptMap = list.stream().collect(Collectors.toMap(EnsemblTranscript::id, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTranscriptType(String accession) {
        if (ensemblTranscriptMap.size() > 0) {
            return ensemblTranscriptMap.get(accession).type();
        }
        extracted();
        return ensemblTranscriptMap.get(accession).type();
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

