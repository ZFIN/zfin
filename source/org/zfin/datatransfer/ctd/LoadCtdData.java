package org.zfin.datatransfer.ctd;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.DataReportTask;
import org.zfin.ontology.TermExternalReference;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.loadDir;
import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.webrootDirectory;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class LoadCtdData extends AbstractScriptWrapper {

    static {
        options.addOption(loadDir);
        options.addOption(webrootDirectory);
        options.addOption(DataReportTask.jobNameOpt);
    }

    public LoadCtdData() {
    }

    private MeshCasChebiMappings mapping = new MeshCasChebiMappings();

    public static void main(String[] arguments) throws IOException {

        LoadCtdData load = new LoadCtdData();
        load.initAll();
        load.dao = new MeshChebiDAO(HibernateUtil.currentSession());
        //getReferenceCDT();
        load.loadMeshAndChebi();
        load.reportNonJointCasIds();
        load.printOneToOneMeshChebiRelations();
        load.reportOneToNRelations();
        //load.reportOneToManyRelations();
        //getChemGeneInteractionCDT();
        ///load.execute();
        System.exit(0);
    }


    private void printOneToOneMeshChebiRelations() {
        Set<MeshCasChebiRelation> oneToOneRelations = mapping.getOneToOneRelations();
        writeOneToOneFile(oneToOneRelations, "mesh-chebi-one-to-one.csv");

        List<MeshChebiMapping> newRecords = oneToOneRelations.stream().map(LoadCtdData::getMeshChebiMapping).collect(Collectors.toList());
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
        File downloadedFile = new File("CTD_references_20230928080547.csv");
        List<String> pubmedIDs = new ArrayList<>();
        Reader in = new FileReader(downloadedFile);
        Iterable<CSVRecord> records = CSVFormat.EXCEL
            .withHeader(Header.class)
            .parse(in);
        int lineNumber = 0;
        for (CSVRecord record : records) {
            lineNumber++;
            // ignore header info
            if (record.get(0).startsWith("!"))
                continue;
            String id = record.get(Header.PID);
            pubmedIDs.add(id);
        }
        pubmedIDs.remove(0);
        List<String> existingIDs = new ArrayList<>();
        pubmedIDs.forEach(id -> {
            Integer pubID = Integer.parseInt(id);
            List<Publication> publicationByPmid = getPublicationRepository().getPublicationByPmid(pubID);
            if (publicationByPmid != null) {
                publicationByPmid.forEach(publication -> {
                    existingIDs.add(publication.getZdbID());
                });
            }
        });

        System.out.println("Number of records: " + pubmedIDs.size());
    }

    private void loadMeshAndChebi() throws IOException {
        downloadChemicalFile();
        List<MeshCasChebiRelation> relations = new ArrayList<>();

        Map<String, String> meshToCasMap = new HashMap<>();
        Reader in = new FileReader(downloadedChemicalFile);
        Iterable<CSVRecord> records = CSVFormat.EXCEL
            .withHeader(CHEMICALS.class)
            .parse(in);
        int lineNumber = 0;
        for (CSVRecord record : records) {
            lineNumber++;
            if (lineNumber == 1)
                continue;
            // ignore header info
            if (record.get(0).startsWith("!") || record.get(0).startsWith("#"))
                continue;
            MeshCasChebiRelation relation = new MeshCasChebiRelation();
            relation.setMesh(record.get(CHEMICALS.MESH));
            relation.setMeshName(record.get(CHEMICALS.NAME));
            if (StringUtils.isNotEmpty(record.get(CHEMICALS.CAS))) {
                relation.setCas("CAS:" + record.get(CHEMICALS.CAS));
            }
            meshToCasMap.put(record.get(CHEMICALS.MESH), record.get(CHEMICALS.CAS));
            relations.add(relation);
        }
        mapping.addMeshCasRelations(relations);
        mapping.addChebiCasRelations(getChebiToCasMapping());
        System.out.println("Unique Chebi-Cas relations: " + mapping.getChebiCasUnique().size());
        System.out.println("Unique Cas-Chebi relations: " + mapping.getCasChebiUnique().size());
    }

    protected DownloadService downloadService = new DownloadService();

    private File downloadedChemicalFile;

    private void downloadChemicalFile() {
        String fileName = "CTD_chemicals.csv";
        String url = "https://ctdbase.org/reports/" + fileName + ".gz";
        try {
            downloadedChemicalFile = downloadService.downloadFile(new File(fileName), new URL(url), false);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    MeshChebiDAO dao;

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
        mapping.getChebiCasMulti()
            .forEach((id, meshCasChebiRelations) ->
                meshCasChebiRelations.forEach(relation -> outputChebiCasMultiple.add(getChebiCasMeshOutputRecord(relation))));
        writeOutMultipleFile(outputChebiCasMultiple, "chebi-cas-mapping-multiple.csv");
        mapping.getCasChebiMulti()
            .forEach((id, meshCasChebiRelations) ->
                meshCasChebiRelations.forEach(relation -> outputChebiCasMultiple.add(getCasChebiMeshOutputRecord(relation))));
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

    private void getChemGeneInteractionCDT() throws IOException {
        File downloadedFile = new File("CTD_chem_gene_ixns.csv");
        List<String> pubmedIDs = new ArrayList<>();
        List<CSVRecord> recordsDR = new ArrayList<>();
        Reader in = new FileReader(downloadedFile);
        Iterable<CSVRecord> records = CSVFormat.EXCEL
            .withHeader(HeaderChem.class)
            .parse(in);
        int lineNumber = 0;
        for (CSVRecord record : records) {
            lineNumber++;
            // ignore header info
            if (record.get(0).startsWith("!"))
                continue;
            String id = record.get(HeaderChem.PUBMED_ID);
            String org = record.get(HeaderChem.ORGANISM_ID);
            if (id != null && org.contains("7955"))
                recordsDR.add(record);
        }
        System.out.println("Number of Danio records: " + recordsDR.size());

//        pubmedIDs.remove(0);
        // get records with non-null CAS Ids.
        List<String> existingIDs = new ArrayList<>();
        recordsDR.stream().filter(record -> StringUtils.isNotEmpty(record.get(HeaderChem.CAS_RN))).forEach(record -> {
            String casID = record.get(HeaderChem.CAS_RN);
            TermExternalReference ref = getOntologyRepository().getTermExternalReference(casID, "CAS");
            if (ref != null)
                existingIDs.add(ref.getTerm().getOboID());
        });
        System.out.println("Number of records with identified CAS Chebi terms: " + existingIDs.size());

        List<String> existingIDsWithMesh = new ArrayList<>();
        recordsDR.stream().filter(record -> StringUtils.isNotEmpty(record.get(HeaderChem.CHEMID))).forEach(record -> {
            String meshID = record.get(HeaderChem.CHEMID);
            TermExternalReference ref = getOntologyRepository().getTermExternalReference(meshID, "MESH");
            if (ref != null)
                existingIDsWithMesh.add(ref.getTerm().getOboID());
        });
        System.out.println("Number of records with identified Mesh-Chebi terms: " + existingIDsWithMesh.size());


    }


}

enum CHEMICALS {
    NAME, MESH, CAS, DEFINITION, CITATION, PARENT_IDS, TREE_NUMBERS, PARENT_TREE_NUMBERS, SYNONYS;
}

enum Header {
    PID, AUTHORS, TITLE, YEAR, CITATION, CITED_CHEMICAL, CITED_DISEASES, CITED_GENES, CITED_PHENOTYPE, CITED_AO_TERMS,
    ;
}

enum HeaderChem {
    CHEM_NAME, CHEMID, CAS_RN, GENE_SYMBOL, GENE_ID, GENE_FROM, ORGANISM, ORGANISM_ID, INTERACTION, INTERACTION_ACTIONS, PUBMED_ID;

    String val;

}


