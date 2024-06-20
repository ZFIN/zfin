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
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.services.VocabularyService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.*;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.marker.presentation.RelatedTranscriptDisplay;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;
import org.zfin.sequence.service.TranscriptService;
import org.zfin.util.FileUtil;

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

public class EnsemblTranscriptFastaReadProcess {

    private static final String baseUrl = "https://rest.ensembl.org";

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptFastaReadProcess loader = new EnsemblTranscriptFastaReadProcess();
        loader.init();
    }

    private Map<String, List<RichSequence>> allEnsemblProvidedGeneMap;

    Map<Marker, List<TranscriptDBLink>> geneEnsdartMap;
    Map<String, MarkerDBLink> ensdargMap;


    public void init() throws IOException {
        loadSequenceMapFromDownloadFile();

        // <ensdargID, DBLink>
        getMarkerDBLinksWithVegaGenbankNoEnsemblAccessions();
        getMarkerDBLinksWithVegaEnsemblOnlyAccessions();
        getMarkerDBLinksWithGenbankEnsemblOnlyAccessions();

        ensdargMap = getMarkerDBLinksWithVegaGenbankEnsemblAccessions();
        geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        createReportFiles(geneEnsdartMap, new ArrayList<>(ensdargMap.keySet()));
        System.out.println("Total Number of Genes with Ensembl Transcripts In ZFIN: " + geneEnsdartMap.size());

        List<Marker> ensemblGenesToBeImported = getEnsemblAccessionsToBeImported(geneEnsdartMap);
        createTranscriptRecords(ensemblGenesToBeImported);
        System.exit(0);

        allEnsemblProvidedGeneMap.entrySet().removeIf(entry -> !ensdargMap.containsKey(entry.getKey()));
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file matching a gene record in ZFIN: " + allEnsemblProvidedGeneMap.size());

        // remove accession without any new transcripts left
        allEnsemblProvidedGeneMap.entrySet().removeIf(entry -> entry.getValue().size() == 0);
        System.out.println("Total Number of Ensembl Genes with new transcripts in FASTA file matching a gene record in ZFIN: " + allEnsemblProvidedGeneMap.size());
        System.out.println("Total Number of Ensembl Transcripts missing in ZFIN: " + allEnsemblProvidedGeneMap.values().stream().map(List::size).reduce(0, Integer::sum));

        allEnsemblProvidedGeneMap.forEach((s, richSequences) -> richSequences.forEach(richSequence -> System.out.println(ensdargMap.get(s).getMarker().getZdbID() + "," + getGeneIdFromVersionedAccession(richSequence.getAccession())))
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

    private void createSingleTranscript(TranscriptRecord transcriptRecord, boolean useDuplicationRenaming) {
        String ensdartID = transcriptRecord.ensdartID;
        RichSequence ensemblSequence = transcriptRecord.richSequence;
        Marker marker = transcriptRecord.marker;

        String transcriptName = getTranscriptName(ensdartID).toLowerCase();
        Marker existingMarker = null;
        if (useDuplicationRenaming) {
            Object[] record = ensdartDuplicationMap.get(ensdartID);
            if (record == null)
                return;
            transcriptName = (String) record[2];

        }
        existingMarker = getMarkerRepository().getMarkerByAbbreviation(transcriptName);

        if (existingMarker != null) {
            EnsemblErrorRecord errorRecord = new EnsemblErrorRecord(ensdartID,
                transcriptName,
                ensemblSequence.length(),
                marker.getZdbID(),
                marker.getAbbreviation(),
                existingMarker.getZdbID(),
                existingMarker.getAbbreviation());
            errorRecords.add(errorRecord);
            return;
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
            if (bioType.equals("retained_intron") || bioType.equals("processed_transcript") || bioType.equals("nonsense_mediated_decay")
                || bioType.equals("unprocessed_pseudogene")) {
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

    private record TranscriptRecord(Marker marker, String ensdartID, RichSequence richSequence) {
    }

    private Map<String, Object[]> ensdartDuplicationMap;

    private void createTranscriptRecords(List<Marker> genesToAddTranscripts) {
        System.out.println("Number of Genes for which transcripts need to be added: " + genesToAddTranscripts.size());
        AtomicInteger index = new AtomicInteger(0);
        List<TranscriptRecord> newTranscriptList = new ArrayList<>();

        genesToAddTranscripts.forEach(marker -> {
            // get all ENSDARG IDs for the given gene (including 1-N)
            List<String> accessionNumbers = getAccessionNumber(marker);
            if (CollectionUtils.isEmpty(accessionNumbers)) {
                return;
            }
            accessionNumbers.forEach(accessionNumber -> {
                // obtain all transcripts from Ensembl for the given ENSDARG ID
                List<RichSequence> transcriptsEnsembl = allEnsemblProvidedGeneMap.get(accessionNumber);
                if (transcriptsEnsembl == null) {
                    return;
                }
                transcriptsEnsembl.forEach(richSequence -> {
                    String ensdartID = getString(richSequence);
                    // only create those ensembl transcripts that do not exist yet in ZFIN
                    if (transcriptExist(marker, ensdartID)) {
                        return;
                    }
                    newTranscriptList.add(new TranscriptRecord(marker, ensdartID, richSequence));
                });
            });
        });

        // find duplicate transcript names
        Map<String, List<String>> transcriptNameMap = getTranscriptNameEnsdartIdsMap(newTranscriptList);
        Map<String, List<String>> duplicateIDMap = writeDuplicatedReport(transcriptNameMap);
        List<String> allduplicatedEnsdartIDs = duplicateIDMap.values().stream().flatMap(Collection::stream).toList();
        String sql = "select * from ensembl_transcript_renaming";
        List<Object[]> results = HibernateUtil.currentSession().createNativeQuery(sql).getResultList();
        ensdartDuplicationMap = results.stream().collect(Collectors.toMap(o -> (String) o[1], Function.identity()));
        List<String> duplicateIDs = new ArrayList<>(allduplicatedEnsdartIDs);
        duplicateIDs.addAll(ensdartDuplicationMap.keySet());
        System.out.println("Number of new Transcript records: " + newTranscriptList.size());
        newTranscriptList.forEach(transcriptRecord -> {
            boolean isDuplicate = false;
            if (duplicateIDs.contains(transcriptRecord.ensdartID)) {
                isDuplicate = true;
            }
            createSingleTranscript(transcriptRecord, isDuplicate);
            if (index.incrementAndGet() % 100 == 0) {
                System.out.print(index + "..");
            }
        });
/*
        sequenceListToBeGenerated.forEach(richSequence -> {
            RichSequence.IOTools.SingleRichSeqIterator iterator = new RichSequence.IOTools.SingleRichSeqIterator(richSequence);
            try {
                write("ensembl-transcripts.1.fasta", iterator);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
*/

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
                vals.add(record.ensdartLength + "");
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
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private Map<String, List<String>> writeDuplicatedReport(Map<String, List<String>> transcriptNameMap) {
        List<String> headerNames = List.of(
            "Ensembl Transcript Name",
            "Ensembl Ensdart IDs"
        );
        Map<String, List<String>> duplicatedTranscriptNameMap = new HashMap<>();
        transcriptNameMap.forEach((transcriptName, ensdartIDs) -> {
            if (ensdartIDs.size() > 1) {
                duplicatedTranscriptNameMap.put(transcriptName, ensdartIDs);
            }
        });

        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(Paths.get("duplicated-ensembl-transcript-name-report.txt"));
            CSVPrinter csvPrinterImportant = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader(headerNames.toArray(String[]::new)));
            duplicatedTranscriptNameMap.forEach((name, ids) -> {
                List<String> vals = new ArrayList<>();
                vals.add(name);
                vals.add(String.join(",", ids));
                Object[] values = vals.toArray();

                try {
                    csvPrinterImportant.printRecord(values);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return duplicatedTranscriptNameMap;
    }

    private Map<String, List<String>> getTranscriptNameEnsdartIdsMap(List<TranscriptRecord> newTranscriptList) {
        // transcriptName, ensdartIDs
        Map<String, List<String>> transcriptNameMap = new HashMap<>();
        newTranscriptList.forEach(transcriptRecord -> {
            String ensdartID = transcriptRecord.ensdartID;
            String transcriptName = getTranscriptName(ensdartID).toLowerCase();
            List<String> ensdartIDs = transcriptNameMap.computeIfAbsent(transcriptName, k -> new ArrayList<>());
            ensdartIDs.add(ensdartID);
        });
        return transcriptNameMap;
    }

    private boolean transcriptExist(Marker marker, String ensdartID) {
        return geneEnsdartMap.get(marker).stream().map(DBLink::getAccessionNumber).toList().contains(ensdartID);
    }

    private List<String> getAccessionNumber(Marker marker) {
        List<Map.Entry<String, MarkerDBLink>> entries = ensdargMap.entrySet().stream().filter(entry -> entry.getValue().getMarker().getZdbID().equals(marker.getZdbID())).toList();
        return entries.stream().map(entry -> entry.getValue().getAccessionNumber()).toList();
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
            listOfGeneInfoToBeImported.add(gene);
        });
        return listOfGeneInfoToBeImported;
    }

    private void createReportFiles(Map<Marker, List<TranscriptDBLink>> geneEnsdartMap, List<String> genesToBeConsidered) throws IOException {
        List<String> listOfGeneIdsWithNoDifference = new ArrayList<>();
        List<String> listOfGeneIdsWithDifferencesEnsemblLongest = new ArrayList<>();
        List<String> listOfGeneIdsWithDifferences = new ArrayList<>();
        List<String> listOfGeneIdsNotFoundInFile = new ArrayList<>();
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("report-transcript-ensembl.txt"));

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

        geneEnsdartMap.forEach((gene, markerDBLink) -> {
            List<String> values = new ArrayList<>(headerNames.size());

            List<LinkDisplay> list = getMarkerRepository().getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE);
            List<LinkDisplay> ensdarg = list.stream().filter(linkDisplay -> linkDisplay.getAccession().startsWith("ENSDARG")).toList();
            if (CollectionUtils.isEmpty(ensdarg))
                return;
            String ensdargAccession = ensdarg.get(0).getAccession();
            if (!genesToBeConsidered.contains(gene.getZdbID())) {
                return;
            }
            List<RichSequence> transcriptsEnsembl = allEnsemblProvidedGeneMap.get(ensdargAccession);
            if (transcriptsEnsembl == null) {
                listOfGeneIdsNotFoundInFile.add(ensdargAccession);
                return;
            }
            Set<String> zfinAccessions = markerDBLink.stream().map(TranscriptDBLink::getAccessionNumber).collect(Collectors.toSet());
            Set<String> ensemblAccessions = transcriptsEnsembl.stream().map(EnsemblTranscriptFastaReadProcess::getString).collect(Collectors.toSet());
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
                    csvPrinterImportant.printRecord((Object[]) values1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private static String getString(RichSequence richSequence) {
        return richSequence.getAccession().split("\\.")[0];
    }

    private Map<String, MarkerDBLink> getMarkerDBLinksWithVegaGenbankNoEnsemblAccessions() {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        // vega gene list
        List<String> vegaGeneList = vegaList.stream().map(LinkDisplay::getAssociatedGeneID).toList();
        genbankList.removeIf(markerDBLink -> !vegaGeneList.contains(markerDBLink.getMarker().getZdbID()));

        List<String> strings = ensdargList.stream().map(markerDB -> markerDB.getMarker().getZdbID()).toList();
        genbankList.removeIf(markerDBLink -> strings.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Genes that have a Vega and GenBank Gene but no ensembl gene: " + genbankList.size());
        Map<String, MarkerDBLink> ensdargMap = genbankList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));
        try {
            createReportNoEnsembl(ensdargMap, "report-transcript-no-ensembl.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ensdargMap;
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

    private void getMarkerDBLinksWithVegaEnsemblOnlyAccessions() {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        // vega gene list
        List<String> vegaGeneList = vegaList.stream().map(LinkDisplay::getAssociatedGeneID).toList();
        // genbank gene list
        List<String> genbankGeneList = genbankList.stream().map(markerDBLink1 -> markerDBLink1.getMarker().getZdbID()).toList();
        ensdargList.removeIf(markerDBLink -> !vegaGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that also have a Vega Gene: " + ensdargList.size());
        ensdargList.removeIf(markerDBLink -> genbankGeneList.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Ensembl Genes that have a Vega and no GenBank Gene: " + ensdargList.size());
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
            Collectors.toMap(DBLink::getAccessionNumber, Function.identity(), (existing, replacement) -> existing));
        try {
            createReportNoEnsembl(ensdargMap, "report-transcript-ensembl-vega-no-genbank");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

