package org.zfin.sequence.load;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.Species;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.services.VocabularyService;
import org.zfin.infrastructure.ActiveData;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static org.zfin.framework.services.VocabularyEnum.TRANSCRIPT_ANNOTATION_METHOD;
import static org.zfin.marker.Marker.Type.TSCRIPT;
import static org.zfin.marker.TranscriptType.Type.MRNA;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE;
import static org.zfin.sequence.load.EnsemblLoadAction.HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G;
import static org.zfin.sequence.load.EnsemblLoadAction.ZFIN_WWW;
import static org.zfin.sequence.load.LoadAction.SubType.*;

public class EnsemblTranscriptFastaReadProcess extends EnsemblTranscriptBase {

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
        super.init();
        loadSequenceMapFromDownloadFile();

        // <ensdargID, DBLink>
        ensdargMap = getMarkerDBLinksWithVegaGenbankEnsemblAccessions();
        ensdargMap.putAll(getMarkerDBLinksWithGenbankEnsemblOnlyAccessions());
        ensdargMap.putAll(getMarkerDBLinksWithEnsemblAccessionsOnly());
        geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        // add all marker that do not have a single Ensembl transcript associated.
        ensdargMap.values().forEach(markerDBLink -> {
            if (!geneEnsdartMap.containsKey(markerDBLink.getMarker())) {
                geneEnsdartMap.put(markerDBLink.getMarker(), null);
            }
        });
        createReportFiles(geneEnsdartMap, new ArrayList<>(ensdargMap.keySet()));
        System.out.println("Total Number of Genes with Ensembl Transcripts In ZFIN: " + geneEnsdartMap.size());

        List<Marker> ensemblGenesToBeImported = getEnsemblAccessionsToBeImported(geneEnsdartMap);
        createTranscriptRecords(ensemblGenesToBeImported);
        addEnsemblRecordToTranscript();
        removeEnsemblRecordsFromTranscript();


        return;
/*

        allEnsemblProvidedGeneMap.entrySet().removeIf(entry -> !ensdargMap.containsKey(entry.getKey()));
        System.out.println("Total Number of Ensembl Genes with transcripts in FASTA file matching a gene record in ZFIN: " + allEnsemblProvidedGeneMap.size());

        // remove accession without any new transcripts left
        allEnsemblProvidedGeneMap.entrySet().removeIf(entry -> entry.getValue().size() == 0);
        System.out.println("Total Number of Ensembl Genes with new transcripts in FASTA file matching a gene record in ZFIN: " + allEnsemblProvidedGeneMap.size());
        System.out.println("Total Number of Ensembl Transcripts missing in ZFIN: " + allEnsemblProvidedGeneMap.values().stream().map(List::size).reduce(0, Integer::sum));

        allEnsemblProvidedGeneMap.forEach((s, richSequences) -> richSequences.forEach(richSequence -> System.out.println(ensdargMap.get(s).getMarker().getZdbID() + "," + getGeneIdFromVersionedAccession(richSequence.getAccession())))
        );
*/
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
        Marker existingMarker;
        if (useDuplicationRenaming) {
            Object[] record = ensdartDuplicationMap.get(ensdartID);
            if (record == null) {
                LoadLink newTranscriptLink = new LoadLink(transcriptRecord.ensdartID, "null");
                LoadAction newTranscript = new LoadAction(LoadAction.Type.WARNING, NO_NAME_FOR_TRANSCRIPT_FOUND, transcriptRecord.ensdartID, marker.zdbID, "This ENSDART is not loaded into ZFIN", 0, new HashMap<>());
                newTranscript.addLink(newTranscriptLink);
                actions.add(newTranscript);
                return;
            }
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
            LoadLink newTranscriptLink = new LoadLink(transcriptRecord.ensdartID, "null");
            HashMap<String, String> links = new HashMap<>();
            links.put("transcriptName", transcriptName);
            links.put("transcriptID", existingMarker.getZdbID());
            LoadAction newTranscript = new LoadAction(LoadAction.Type.ERROR, TRANSCRIPT_EXISTS, transcriptRecord.ensdartID, marker.getZdbID(), "This ENSDARG currently is not loaded into ZFIN", 0, links);
            newTranscript.addLink(newTranscriptLink);
            actions.add(newTranscript);
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
        if (!(bioType.equals("protein_coding")
              || bioType.equals("pseudogene")
              || bioType.equals("lincRNA")
              || bioType.equals("miRNA")
              || bioType.equals("misc_RNA")
              || bioType.equals("scaRNA")
              || bioType.equals("antisense")
              || bioType.equals("snoRNA"))) {
            if (bioType.equals("retained_intron") || bioType.equals("processed_transcript") || bioType.equals("nonsense_mediated_decay")
                || bioType.equals("unprocessed_pseudogene") || bioType.equals("ribozyme")) {
                LoadLink unsupprtedBioTypeLink = new LoadLink(transcriptRecord.ensdartID, "https://zfin.org/" + transcript.getZdbID());
                HashMap<String, String> columns = new HashMap<>();
                columns.put("biotype", bioType);
                LoadAction newTranscript = new LoadAction(LoadAction.Type.WARNING, UNSUPPTORTED_BIOTYPE, transcriptRecord.ensdartID, "", "This ENSDARG is not loaded into ZFIN: biotype = " + bioType, 0, columns);
                newTranscript.addLink(unsupprtedBioTypeLink);
                actions.add(newTranscript);
                return;
            }
            throw new RuntimeException("Could not map biotype " + bioType + " for transcript " + transcriptName + " with accession " + ensdartID);
        }
        transcript.setTranscriptType(transcriptTypeForName);

        HibernateUtil.createTransaction();
        try {
            persistTranscriptInfo(ensdartID, ensemblSequence, marker, transcript);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            System.out.println("Failed to save transcript: " + ensdartID + " [" + transcript.getAbbreviation() + "]");
            HibernateUtil.rollbackTransaction();
            System.out.println(e);
            return;
        }
        addLoadAction(ensdartID, transcript.getZdbID(), transcriptRecord.richSequence, LoadAction.Type.LOAD, ENSDART_LOADED);
    }

    private void persistTranscriptInfo(String ensdartID, RichSequence ensemblSequence, Marker marker, Transcript transcript) {
        MarkerRelationship relationship = new MarkerRelationship();
        if (marker != null) {
            relationship.setFirstMarker(marker);
            relationship.setSecondMarker(transcript);
            relationship.setType(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
            relationship.setMarkerRelationshipType(markerRelationshipType);
            marker.getFirstMarkerRelationships().add(relationship);
        }

        TranscriptDBLink transcriptDBLink = new TranscriptDBLink();
        transcriptDBLink.setTranscript(transcript);
        transcriptDBLink.setAccessionNumber(ensdartID);
        transcriptDBLink.setLength(ensemblSequence.length());
        database.getDisplayGroups().add(nucleotideSequ);
        transcriptDBLink.setReferenceDatabase(database);
        Locale locale = new Locale("en", "US");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        String date = dateFormat.format(new Date());
        transcriptDBLink.setLinkInfo("Ensembl Load from " + date);

        transcript.setStrain(tu);
        transcript.setAnnotationMethod(annotationMethod);
        if (marker != null) {
            HibernateUtil.currentSession().save(transcript);
            HibernateUtil.currentSession().save(relationship);
            PublicationAttribution attribution = getInfrastructureRepository().insertStandardPubAttribution(transcript.getZdbID(), pub);
            // attribute markerRelationship
            relationship.setPublications(Set.of(attribution));
        } else {
            PublicationAttribution attribution = getInfrastructureRepository().insertStandardPubAttribution(transcript.getZdbID(), pub);
        }
        HibernateUtil.currentSession().save(transcriptDBLink);
    }

    private record TranscriptRecord(Marker marker, String ensdartID, RichSequence richSequence) {
    }

    private Map<String, Object[]> ensdartDuplicationMap;
    public Set<LoadAction> actions = new HashSet<>();

    private void createTranscriptRecords(List<Marker> genesToAddTranscripts) {
        System.out.println("Number of Genes for which transcripts need to be added: " + genesToAddTranscripts.size());

        AtomicInteger index = new AtomicInteger(0);
        List<TranscriptRecord> newTranscriptList = new ArrayList<>();

        genesToAddTranscripts.forEach(marker -> {
            // get all ENSDARG IDs for the given gene (including 1-N)
            List<String> ensdargIDs = getAccessionNumber(marker);
            if (CollectionUtils.isEmpty(ensdargIDs)) {
                return;
            }
            if (ensdargIDs.size() > 1) {
                String accessionConcatenation = String.join(" | ", ensdargIDs);
                LoadAction loadAction = new LoadAction(LoadAction.Type.WARNING, MULTIPLE_GENES_PER_ACCESSION, accessionConcatenation, marker.getZdbID(), "No transcripts are loaded into ZFIN for a given ENSDARG", 0, new HashMap<>());
                ensdargIDs.forEach(accession -> {
                    LoadLink link = new LoadLink(accession, HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G + accession);
                    loadAction.addLink(link);
                });
                LoadLink zfinLink = new LoadLink(marker.getZdbID(), "https://zfin.org/" + marker.getZdbID());
                loadAction.addLink(zfinLink);
                actions.add(loadAction);
                return;
            }
            ensdargIDs.forEach(accessionNumber -> {
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
            boolean isDuplicate = duplicateIDs.contains(transcriptRecord.ensdartID);
            createSingleTranscript(transcriptRecord, isDuplicate);
            if (index.incrementAndGet() % 100 == 0) {
                System.out.print(index + "..");
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

    private void addEnsemblRecordToTranscript() {

        String sql = "select eta_ensdart_id, eta_mrkr_zdb_id from ensembl_transcript_add";
        List<Object[]> results = HibernateUtil.currentSession().createNativeQuery(sql).getResultList();
        results.forEach(objects -> {
            String ensdartID = (String) objects[0];
            String transcriptID = (String) objects[1];
            Transcript transcript = getMarkerRepository().getTranscriptByZdbID(transcriptID);
            if (ensdartExists(ensdartID)) {
                return;
            }

            List<RichSequence> ensemblTranscripts = allEnsemblProvidedGeneMap.values().stream().flatMap(Collection::stream).toList().stream().filter(richSequence -> getString(richSequence).equals(ensdartID)).toList();
            if (CollectionUtils.isEmpty(ensemblTranscripts)) {
                return;
            }
            RichSequence eTranscript = ensemblTranscripts.get(0);
            HibernateUtil.createTransaction();
            try {
                persistTranscriptInfo(ensdartID, eTranscript, null, transcript);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                System.out.println("Failed to add DB_LINK to transcript: " + ensdartID + " [" + transcript.getAbbreviation() + "]");
                HibernateUtil.rollbackTransaction();
                return;
            }
            addLoadAction(ensdartID, transcriptID, eTranscript, LoadAction.Type.LOAD, ENSDART_ADDED);
        });

    }

    private void removeEnsemblRecordsFromTranscript() {

        String sql = "select etd_ensdart_id, etd_mrkr_zdb_id from ensembl_transcript_delete";
        List<Object[]> results = HibernateUtil.currentSession().createNativeQuery(sql).getResultList();
        results.forEach(objects -> {
            String ensdartID = (String) objects[0];
            String transcriptID = (String) objects[1];
            Transcript transcript = getMarkerRepository().getTranscriptByZdbID(transcriptID);
            transcript.getTranscriptDBLinks().stream()
                .filter(transcriptDBLink -> transcriptDBLink.getAccessionNumber().equals(ensdartID))
                .forEach(transcriptDBLink -> {
                    HibernateUtil.createTransaction();
                    try {
                        ActiveData activeData = getInfrastructureRepository().getActiveData(transcriptDBLink.getZdbID());
                        HibernateUtil.currentSession().delete(activeData);
                        HibernateUtil.flushAndCommitCurrentSession();
                    } catch (Exception e) {
                        System.out.println("Failed to delete DB_LINK from transcript: " + ensdartID + " [" + transcript.getAbbreviation() + "]");
                        HibernateUtil.rollbackTransaction();
                        System.out.println(e.getMessage());
                        return;
                    }
                    addLoadAction(ensdartID, transcriptID, null, LoadAction.Type.DELETE, ENSDART_REMOVED);
                });


        });

    }

    private boolean ensdartExists(String accession) {
        return geneEnsdartMap.values().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(transcriptDBLink -> transcriptDBLink.getAccessionNumber().equals(accession));
    }

    private void addLoadAction(String ensdartID, String transcriptID, RichSequence eTranscript, LoadAction.Type type, LoadAction.SubType subType) {
        LoadLink loadLink = new LoadLink(ensdartID, HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G + transcriptID);
        LoadAction loadAction = new LoadAction(type, subType, ensdartID, transcriptID, "", 0, new HashMap<>());
        loadAction.addLink(loadLink);
        loadAction.addLink(new LoadLink(transcriptID, ZFIN_WWW + transcriptID));
        if (eTranscript != null) {
            loadAction.setDetails(eTranscript.getDescription());
        }
        actions.add(loadAction);
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
        transcriptNameMap.forEach((transcriptName, ids) -> {

            LoadLink loadLink = new LoadLink(transcriptName, "");
            LoadAction loadAction = new LoadAction(LoadAction.Type.WARNING, ENSEMBL_TRANSCRIPTS_DUPLICATE_PER_NAME, transcriptName, String.join(",", ids), "", 0, new HashMap<>());
            loadAction.addLink(loadLink);
            loadAction.addLink(loadLink);
            if (ids.size() > 1) {
                actions.add(loadAction);
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
        List<TranscriptDBLink> transcriptDBLinks = geneEnsdartMap.get(marker);
        if (CollectionUtils.isEmpty(transcriptDBLinks)) {
            return false;
        }

/*
        transcriptDBLinks.stream().map(transcriptDBLink -> transcriptDBLink.getTranscript().getAbbreviation()).toList().forEach(s -> {
            System.out.println(marker.getZdbID() + " " + ensdartID + " " + s);
        });
*/
        return transcriptDBLinks.stream().map(DBLink::getAccessionNumber).toList().contains(ensdartID);
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

    private Map<String, MarkerDBLink> getMarkerDBLinksWithEnsemblAccessionsOnly() {
        List<MarkerDBLink> ensdargList = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        List<LinkDisplay> vegaList = getMarkerRepository().getAllVegaGeneDBLinksTranscript();
        List<MarkerDBLink> genbankList = getSequenceRepository().getAllGenbankGenes();
        // vega gene list
        List<String> vegaGeneList = vegaList.stream().map(LinkDisplay::getAssociatedGeneID).toList();
        ensdargList.removeIf(markerDBLink -> vegaGeneList.contains(markerDBLink.getMarker().getZdbID()));

        List<String> geneBankIDs = genbankList.stream().map(markerDB -> markerDB.getMarker().getZdbID()).toList();
        ensdargList.removeIf(markerDBLink -> geneBankIDs.contains(markerDBLink.getMarker().getZdbID()));
        System.out.println("Number of Genes that have no Vega and no GenBank ID: " + ensdargList.size());
        Map<String, MarkerDBLink> ensdargMap = ensdargList.stream().collect(
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
        List<String> ensdargGeneList = ensdargList.stream().map(markerDBLink -> markerDBLink.getMarker().getZdbID()).toList();

        //getEnsemblOnlyGenes(vegaGeneList, genbankGeneList, ensdargGeneList, ensdargList);
        //getNcbiOnlyGenes(vegaGeneList, genbankGeneList, ensdargGeneList, genbankList);
        //getVegaOnlyGenes(vegaList, vegaGeneList, genbankGeneList, ensdargGeneList);
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

    private static void getVegaOnlyGenes(List<LinkDisplay> vegaList, List<String> vegaGeneList, List<String> genbankGeneList, List<String> ensdargGeneList) {
        List<String> vegaOnly = new ArrayList<>();
        vegaGeneList.forEach(vegaID -> {
            if (!genbankGeneList.contains(vegaID) && !ensdargGeneList.contains(vegaID)) {
                vegaOnly.add(vegaID);
            }
        });


        Map<Marker, List<TranscriptDBLink>> geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();

        vegaOnly.forEach(s -> {
            Marker gene = getMarkerRepository().getMarkerByID(s);
            List<TranscriptDBLink> transcriptList = new ArrayList<>();
            geneEnsdartMap.entrySet().stream().filter(markerListEntry -> markerListEntry.getKey().getZdbID().equals(gene.getZdbID()))
                .forEach(markerListEntry -> {
                    transcriptList.addAll(markerListEntry.getValue());
                    Set<Genotype> genoList = new HashSet<>();
                    transcriptList.forEach(transcriptDBLink -> {
                        genoList.add(transcriptDBLink.getTranscript().getStrain());
                    });
                    if (CollectionUtils.isNotEmpty(genoList) && genoList.size() > 1) {
                        System.out.println("More than one type of strain in the set of transcripts per gene: " + gene.getZdbID());
                    }
                    if (CollectionUtils.isNotEmpty(genoList) && genoList.size() == 1) {
                        AtomicReference<String> vega = new AtomicReference<>();
                        vegaList.forEach(linkDisplay -> {
                            if (linkDisplay.getAssociatedGeneID().equals(gene.getZdbID()) && linkDisplay.getAccession().startsWith("OTTDARG")) {
                                vega.set(linkDisplay.getAccession());
                            }
                        });
                        Genotype next = genoList.iterator().next();
                        if (next != null && next.getHandle().equals("TU")) {
                            System.out.println(s + "\t" + gene.getAbbreviation() + "\t" + vega.get());
                        }
                    }
                });
        });
    }

    private static List<String> getEnsemblOnlyGenes(List<String> vegaGeneList, List<String> genbankGeneList, List<String> ensdargGeneList, List<MarkerDBLink> ensdargList) {
        List<String> ensdargOnly = new ArrayList<>();
        ensdargGeneList.forEach(ensdarg -> {
            if (!genbankGeneList.contains(ensdarg) && !vegaGeneList.contains(ensdarg)) {
                ensdargOnly.add(ensdarg);
            }
        });
        ensdargOnly.forEach(id -> {
            Marker gene = getMarkerRepository().getMarkerByID(id);
            AtomicReference<String> ensdarg = new AtomicReference<>();
            ensdargList.forEach(linkDisplay -> {
                if (linkDisplay.getMarker().getZdbID().equals(id)) {
                    ensdarg.set(linkDisplay.getAccessionNumber());
                }
            });
            System.out.println(id + "\t" + gene.getAbbreviation() + "\t" + ensdarg.get());
        });
        return ensdargOnly;
    }

    private static List<String> getNcbiOnlyGenes(List<String> vegaGeneList, List<String> genbankGeneList, List<String> ensdargGeneList, List<MarkerDBLink> ncbiList) {
        List<String> ncbiOnly = new ArrayList<>();
        genbankGeneList.forEach(ncbiID -> {
            if (!ensdargGeneList.contains(ncbiID) && !vegaGeneList.contains(ncbiID)) {
                ncbiOnly.add(ncbiID);
            }
        });
        ncbiOnly.forEach(id -> {
            Marker gene = getMarkerRepository().getMarkerByID(id);
            AtomicReference<String> ensdarg = new AtomicReference<>();
            ncbiList.forEach(linkDisplay -> {
                if (linkDisplay.getMarker().getZdbID().equals(id)) {
                    ensdarg.set(linkDisplay.getAccessionNumber());
                }
            });
            System.out.println(id + "\t" + gene.getAbbreviation() + "\t" + ensdarg.get());
        });
        return ncbiOnly;
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

    private static String getGeneIdFromVersionedAccession(String accession) {
        return accession.split("\\.")[0];
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
        addLoadAction();
        return ensemblTranscriptMap.get(accession).name();
    }

    private void addLoadAction() {
        try {
            List<EnsemblTranscript> list = getEnsemblBioMartRecords();
            ensemblTranscriptMap = list.stream().collect(Collectors.toMap(EnsemblTranscript::id, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTranscriptType(String accession) {
        if (ensemblTranscriptMap.size() > 0) {
            return ensemblTranscriptMap.get(accession).type();
        }
        addLoadAction();
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

    // This list: ensdartID, name and type should be retrieved directly from ensembl in the future.
    // We are using the name suggested from Ensembl unless specified in the
    public List<EnsemblTranscript> getEnsemblBioMartRecords() {
        List<EnsemblTranscript> transcripts;
        String sql = "select * from transcript_ensembl_name";
        List<Object[]> results = HibernateUtil.currentSession().createNativeQuery(sql).getResultList();
        transcripts = results.stream().map(objects -> new EnsemblTranscript((String) objects[0], (String) objects[1], (String) objects[2])).toList();
        System.out.println("Found " + transcripts.size() + " transcript records in BioMart export file for finding the transcript name by ensdart ID");
        return transcripts;
    }

    private static final String[] headers = {"Transcript stable ID", "Transcript name", "Transcript typ"};

    private record EnsemblTranscript(String id, String name, String type) {
    }

    private record EnsemblErrorRecord(String ensdartID, String ensdartName, int ensdartLength, String zfinID, String zfinName, String zfinIDExisting, String zfinNameExisting) {
    }
}

