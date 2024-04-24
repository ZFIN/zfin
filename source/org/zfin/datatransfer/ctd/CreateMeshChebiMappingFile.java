package org.zfin.datatransfer.ctd;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.zfin.curation.PublicationNote;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.DataReportTask;
import org.zfin.ontology.TermExternalReference;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.loadDir;
import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.webrootDirectory;
import static org.zfin.repository.RepositoryFactory.*;

public class CreateMeshChebiMappingFile extends AbstractScriptWrapper {

    static {
        options.addOption(loadDir);
        options.addOption(webrootDirectory);
        options.addOption(DataReportTask.jobNameOpt);
    }

    public CreateMeshChebiMappingFile() {
    }

    private MeshCasChebiMappings mapping = new MeshCasChebiMappings();

    public static void main(String[] arguments) throws IOException {

        CreateMeshChebiMappingFile load = new CreateMeshChebiMappingFile();
        load.executeExport();
        System.exit(0);
    }

    private void executeExport() {
        initAll();
        dao = new MeshChebiDAO(HibernateUtil.currentSession());
        List<MeshChebiMapping> meshChebiList = dao.forceRetrieveAll();
        createExportFile(meshChebiList);
    }

    private void createExportFile(List<MeshChebiMapping> meshCehbiList) {
        List<String> headerNames = List.of(
            "subject_id",
            "predicate_id",
            "object_id",
            "mapping_justification",
            "subject_label",
            "object_label"
        );

        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(Paths.get("mesh-chebi-mapping.tsv"));
            CSVPrinter csvPrinterImportant = new CSVPrinter(writer, CSVFormat.TDF
                .withHeader(headerNames.toArray(String[]::new)));
            meshCehbiList.forEach((map) -> {
                List<String> vals = new ArrayList<>();
                vals.add(map.getMeshID());
                vals.add("skos:" + map.getPredicate().getName() + "Match");
                vals.add(map.getChebiID());
                vals.add("semapv:" + map.getMappingJustification().getName());
                vals.add(map.getMeshName());
                vals.add(getOntologyRepository().getTermByOboID(map.getChebiID()).getTermName());
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
    }


    private void printOneToOneMeshChebiRelations() {
        Set<MeshCasChebiRelation> oneToOneRelations = mapping.getOneToOneRelations();
        writeOneToOneFile(oneToOneRelations, "mesh-chebi-one-to-one.csv");

        List<MeshChebiMapping> newRecords = oneToOneRelations.stream().map(CreateMeshChebiMappingFile::getMeshChebiMapping).collect(Collectors.toList());
        HibernateUtil.createTransaction();
        List<MeshChebiMapping> oldRecords = dao.forceRetrieveAll();
        oldRecords.removeAll(newRecords);
        // remove records that are not found in the ingest file
        for (MeshChebiMapping meshChebiMapping : oldRecords) {
            dao.delete(meshChebiMapping);
        }
        List<MeshChebiMapping> updatedOldRecords = dao.forceRetrieveAll();
        newRecords.removeAll(updatedOldRecords);

        // add records that are only found in the ingest file
        newRecords.forEach(relation -> dao.persist(relation));
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private static MeshChebiMapping getMeshChebiMapping(MeshCasChebiRelation relation) {
        MeshChebiMapping mcMapping = new MeshChebiMapping();
        mcMapping.setMeshID(relation.getMesh());
        mcMapping.setMeshName(relation.getMeshName());
        mcMapping.setChebiID(relation.getChebi());
        mcMapping.setCasID(relation.getCas());
        return mcMapping;
    }

    private void reportNonJointCasIds() {
        Set<String> meshCasIDs = mapping.getCasMeshMap().keySet();
        Set<String> chebiCasIDs = mapping.getCasChebiMap().keySet();
        System.out.println("Unique Chebi-Cas relations: " + mapping.getChebiCasUnique().size());

        List<String> nonJoinedCasIDs = new ArrayList<>(chebiCasIDs);
        nonJoinedCasIDs.removeAll(meshCasIDs);
        writeOutFile(nonJoinedCasIDs, "chebi-cas-not-in-mesh-cas.txt");

        nonJoinedCasIDs = new ArrayList<>(meshCasIDs);
        nonJoinedCasIDs.removeAll(chebiCasIDs);
        writeOutFile(nonJoinedCasIDs, "mesh-cas-not-in-chebi-cas.txt");

    }

    private void getReferenceCDT() throws IOException {
        downloadPublicationFile();
        List<String> pubmedIDs = new ArrayList<>();
        Reader in = new FileReader(downloadedPublicationFile);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader(Header.class).parse(in);
        int lineNumber = 0;
        for (CSVRecord record : records) {
            lineNumber++;
            // ignore header info
            if (record.get(0).startsWith("!")) continue;
            String id = record.get(Header.PID);
            pubmedIDs.add(id);
        }
        pubmedIDs.remove(0);

        List<String> publicationsNotFound = new ArrayList<>();
        List<String> existingIDs = new ArrayList<>();
        List<PublicationCtd> newPubCtds = new ArrayList<>();
        List<PublicationNote> noteList = new ArrayList<>();
        pubmedIDs.forEach(id -> {
            Integer pubID = Integer.parseInt(id);
            List<Publication> publicationByPmid = getPublicationRepository().getPublicationByPmid(pubID);
            if (publicationByPmid != null) {
                publicationByPmid.forEach(publication -> {
                    existingIDs.add(publication.getZdbID());
                    PublicationCtd pubCtd = new PublicationCtd();
                    pubCtd.setPublication(publication);
                    pubCtd.setCtdID(String.valueOf(pubID));
                    PublicationNote note = new PublicationNote();
                    note.setPublication(publication);
                    note.setDate(new Date());
                    note.setText("Curated by CTD");
                    // Ceri
                    note.setCurator(getProfileRepository().getPerson("ZDB-PERS-030612-1"));
                    noteList.add(note);
                    newPubCtds.add(pubCtd);
                });
            } else {
                publicationsNotFound.add(id);
            }
        });

        HibernateUtil.createTransaction();
        List<PublicationCtd> existingRecords = pubDao.findAll();
        // only persist new records
        List<Publication> existingPubs = existingRecords.stream().map(PublicationCtd::getPublication).toList();
        newPubCtds.removeAll(existingRecords);
        newPubCtds.forEach(publicationCtd -> pubDao.persist(publicationCtd));
        noteList.stream().filter(publicationNote -> !existingPubs.contains(publicationNote.getPublication())).forEach(publicationNote -> publicationNoteDao.persist(publicationNote));


        HibernateUtil.flushAndCommitCurrentSession();
        //savePublicationCtds();
        System.out.println("Number of new records: " + newPubCtds.size());
        newPubCtds.forEach(publicationCtd -> System.out.println(publicationCtd.getCtdID()));

        System.out.println("\rNumber of records not found in ZFIN: " + publicationsNotFound.size());
        publicationsNotFound.forEach(System.out::println);
    }

    protected DownloadService downloadService = new DownloadService();

    private File downloadedPublicationFile;

    private void downloadPublicationFile() {
        String fileName = "CTD_references.csv";
        String url = "https://ctdbase.org/query.go?type=reference&d-1340579-e=1&action=Search&taxon=TAXON%3A7955&reviewStatus=curated&6578706f7274=1";
        try {
            downloadedPublicationFile = downloadService.downloadFile(new File(fileName), new URL(url), false);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    MeshChebiDAO dao;
    PublicationCtdDAO pubDao;
    PublicationNoteDAO publicationNoteDao;

    private List<MeshCasChebiRelation> getChebiToCasMapping() {

        List<TermExternalReference> referenceList = getOntologyRepository().getAllCasReferences();
        List<MeshCasChebiRelation> relations = new ArrayList<>();

        referenceList.forEach(reference -> {
            MeshCasChebiRelation relation = new MeshCasChebiRelation();
            relation.setChebi(reference.getTerm().getOboID());
            relation.setChebiName(reference.getTerm().getTermName());
            if (reference.getPrefix().equals("CAS")) {
                relation.setCas(reference.getPrefix() + ":" + reference.getAccessionNumber());
            }
            relations.add(relation);
        });
        System.out.println("Number of Chebi-Cas records: " + relations.size());
        return relations;
    }

    private void reportOneToNRelations() {

        Map<String, List<String>> mapCasMultipleNames = mapping.getChebiCasMultipleNames();
        Map<String, List<String>> mapChebiMultiple = mapping.getCasChebiMultiple();
        Map<String, List<MeshCasChebiRelation>> chebiCasMulti = mapping.getChebiCasMulti();
        System.out.println("Number of multiples: Chebi-Cas: " + chebiCasMulti.size());
        System.out.println("Number of multiples: Cas-Chebi: " + mapping.getCasChebiMultiple().size());

        List<String> outputChebiCasMultiple = new ArrayList<>();
        mapping.getChebiCasMulti().forEach((id, meshCasChebiRelations) -> meshCasChebiRelations.forEach(relation -> outputChebiCasMultiple.add(getChebiCasMeshOutputRecord(relation))));
        writeOutMultipleFile(outputChebiCasMultiple, "chebi-cas-mapping-multiple.csv");
        mapping.getCasChebiMulti().forEach((id, meshCasChebiRelations) -> meshCasChebiRelations.forEach(relation -> outputChebiCasMultiple.add(getCasChebiMeshOutputRecord(relation))));
        writeOutMultipleFile(outputChebiCasMultiple, "cas-chebi-mapping-multiple.csv");
        writeOutFile(mapChebiMultiple, "cas-chebi-mapping.txt");
        writeOutFile(mapCasMultipleNames, "cas-chebi-mapping-names.txt");
    }

    private void writeOutMultipleFile(List<String> multipleList, String fileName) {
        Path file = Path.of(fileName);
        StringBuffer buffer = new StringBuffer();
        multipleList.forEach(id -> {
            buffer.append(id);
            buffer.append("\n");
        });
        try {
            Files.writeString(file, buffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getChebiCasMeshOutputRecord(MeshCasChebiRelation relation) {
        StringBuilder record = new StringBuilder();
        StringJoiner joiner = new StringJoiner("\t");
        joiner.add(relation.getChebi());
        joiner.add(relation.getChebiName());
        joiner.add(relation.getCas());
        if (relation.getMesh() != null) {
            joiner.add(relation.getMesh());
            joiner.add(relation.getMeshName());
        }
        record.append(joiner);
        return record.toString();
    }

    private String getCasChebiMeshOutputRecord(MeshCasChebiRelation relation) {
        StringBuilder record = new StringBuilder();
        StringJoiner joiner = new StringJoiner("\t");
        joiner.add(relation.getCas());
        joiner.add(relation.getChebi());
        joiner.add(relation.getChebiName());
        if (relation.getMesh() != null) {
            joiner.add(relation.getMesh());
            joiner.add(relation.getMeshName());
        }
        record.append(joiner);
        return record.toString();
    }

    private static void writeOutFile(Map<String, List<String>> map, String fileName) {
        Path file = Path.of(fileName);
        StringBuffer buffer = new StringBuffer();
        map.forEach((s, strings) -> {
            buffer.append(s + "," + String.join("|", strings));
            buffer.append("\n");
        });
        try {
            Files.writeString(file, buffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeOutFile(Collection<String> list, String fileName) {
        Path file = Path.of(fileName);
        StringBuffer buffer = new StringBuffer();
        list.forEach(id -> {
            buffer.append(id);
            buffer.append("\n");
        });
        try {
            Files.writeString(file, buffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeOneToOneFile(Collection<MeshCasChebiRelation> list, String fileName) {
        Path file = Path.of(fileName);
        StringBuffer buffer = new StringBuffer();
        list.forEach(relation -> {
            StringJoiner joiner = new StringJoiner("\t");
            joiner.add(relation.getChebi());
            joiner.add(relation.getChebiName());
            joiner.add(relation.getMesh());
            joiner.add(relation.getMeshName());
            joiner.add(relation.getCas());
            buffer.append(joiner);
            buffer.append("\n");
        });
        try {
            Files.writeString(file, buffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


