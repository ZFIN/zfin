package org.zfin.uniprot.secondary;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.gwt.root.dto.GoDefaultPublication;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.uniprot.dto.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.Species.Type.ZEBRAFISH;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.ForeignDB.AvailableName.*;
import static org.zfin.sequence.ForeignDBDataType.DataType.DOMAIN;
import static org.zfin.sequence.ForeignDBDataType.DataType.POLYPEPTIDE;
import static org.zfin.sequence.ForeignDBDataType.SuperType.PROTEIN;
import static org.zfin.sequence.ForeignDBDataType.SuperType.SEQUENCE;

/**
 * This class is meant to represent the context in which a load is being performed.
 * Specifically, it is the contents of the database that gets referenced during the load.
 */
@Getter
@Setter
@Log4j2
public class SecondaryLoadContext {
    private static final String SPKW_PUB_ID = "ZDB-PUB-020723-1";
    public static final String GO_TERM_BIOLOGICAL_PROCESS_ID = "ZDB-TERM-091209-6070";
    public static final String GO_TERM_MOLECULAR_FUNCTION_ID = "ZDB-TERM-091209-2432";
    public static final String GO_TERM_CELLULAR_COMPONENT_ID = "ZDB-TERM-091209-4029";

    private Map<String, List<DBLinkSlimDTO>> uniprotDbLinks;
    private Map<String, List<DBLinkSlimDTO>> interproDbLinks;
    private Map<String, List<DBLinkSlimDTO>> ecDbLinks;
    private Map<String, List<DBLinkSlimDTO>> prositeDbLinks;
    private Map<String, List<DBLinkSlimDTO>> pfamDbLinks;
    private Map<String, List<DBLinkSlimDTO>> uniprotDbLinksByGeneZdbID;
//    private Map<DBLinkSlimDTO, DBLinkExternalNoteSlimDTO> externalNotesByUniprotAccession;

//    private List<MarkerGoTermEvidenceSlimDTO> existingMarkerGoTermEvidenceRecordsForSPKW;
    private List<MarkerGoTermEvidenceSlimDTO> existingMarkerGoTermEvidenceRecords;

    private List<SecondaryTerm2GoTerm> interproTranslationRecords;
    private List<SecondaryTerm2GoTerm> ecTranslationRecords;
    private List<InterProProteinDTO> existingInterproDomainRecords;
    private List<ProteinDTO> existingProteinRecords;
    private List<MarkerToProteinDTO> existingMarkerToProteinRecords;
    private List<ProteinToInterproDTO> existingProteinToInterproRecords;
    private List<PdbDTO> existingPdbRecords;

    private Map<String, String> termOntologyMap;


    public static SecondaryLoadContext createFromDBConnection() {
        SecondaryLoadContext loadContext = new SecondaryLoadContext();
        loadContext.initializeContext();

        return loadContext;
    }

    public static SecondaryLoadContext createFromContextFile(String contextInputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SecondaryLoadContext loadContext = mapper.readValue(new File(contextInputFile), SecondaryLoadContext.class);
        return loadContext;
    }

    public void initializeContext() {

        log.info("Load Step 1: Getting Existing Uniprot DB Links");
        initializeUniprotDBLinksFromDatabase();

        log.info("Load Step 2: Getting Existing Interpro DB Links");
        initializeInterproDBLinksFromDatabase();

        log.info("Load Step 3: Getting Existing EC DB Links");
        initializeECDBLinksFromDatabase();

        log.info("Load Step 4: Getting Existing PFAM DB Links");
        initializePfamDBLinksFromDatabase();

        log.info("Load Step 5: Getting Existing PROSITE DB Links");
        initializePrositeDBLinksFromDatabase();

        log.info("Load Step 7: Getting Existing MarkerGoTermEvidence Records");
        initializeMarkerGoTermEvidenceFromDatabase();

        //initializeInterproDomainRecordsFromDatabase
        log.info("Load Step 8: Getting Existing Interpro Domain Entry Records");
        setExistingInterproDomainRecords(fetchExistingInterproDomainRecords());

        //initializeProteinRecordsFromDatabase
        log.info("Load Step 9: Getting Existing Protein Records");
        setExistingProteinRecords(fetchExistingProteinRecords());

        //initializeMarkerToProteinRecordsFromDatabase
        log.info("Load Step 10: Getting Existing Protein Records");
        setExistingMarkerToProteinRecords(fetchExistingMarkerToProteinRecords());

        //initializeProteinToInterproRecordsFromDatabase
        log.info("Load Step 11: Getting Existing Protein to Interpro Records");
        setExistingProteinToInterproRecords(fetchExistingProteinToInterproRecords());

        //initializePdbRecordsFromDatabase
        log.info("Load Step 12: Getting Existing PDB Records");
        setExistingPdbRecords(fetchExistingPdbRecords());

        //initializeTermOntologyMap
        log.info("Load Step 13: Getting Term Ontology Map");
        setTermOntologyMap(fetchTermOntologyMap());
    }

    public void initializeUniprotDBLinksFromDatabase() {
        SequenceRepository sr = getSequenceRepository();

        setUniprotDbLinks(
                convertToDTO(
                        sr.getMarkerDBLinks(
                                sr.getReferenceDatabase(UNIPROTKB, POLYPEPTIDE, SEQUENCE, ZEBRAFISH))));
    }

    public void initializeInterproDBLinksFromDatabase() {
        SequenceRepository sr = getSequenceRepository();

        setInterproDbLinks(
                convertToDTO(
                        sr.getMarkerDBLinks(
                                sr.getReferenceDatabase(INTERPRO, DOMAIN, PROTEIN, ZEBRAFISH))));
    }

    public void initializeECDBLinksFromDatabase() {
        SequenceRepository sr = getSequenceRepository();

        setEcDbLinks(
                convertToDTO(
                        sr.getMarkerDBLinks(
                                sr.getReferenceDatabase(EC, DOMAIN, PROTEIN, ZEBRAFISH))));
    }

    public void initializePfamDBLinksFromDatabase() {
        SequenceRepository sr = getSequenceRepository();

        setPfamDbLinks(
                convertToDTO(
                        sr.getMarkerDBLinks(
                                sr.getReferenceDatabase(PFAM, DOMAIN, PROTEIN, ZEBRAFISH))));
    }

    public void initializePrositeDBLinksFromDatabase() {
        SequenceRepository sr = getSequenceRepository();

        setPrositeDbLinks(
                convertToDTO(
                        sr.getMarkerDBLinks(
                                sr.getReferenceDatabase(PROSITE, DOMAIN, PROTEIN, ZEBRAFISH))));
    }

    public void initializeMarkerGoTermEvidenceFromDatabase() {
        setExistingMarkerGoTermEvidenceRecords(
                MarkerGoTermEvidenceSlimDTO.fromMarkerGoTermEvidences(
                        getMarkerGoTermEvidenceRepository()
                                .getMarkerGoTermEvidencesForPubZdbIDs(List.of(
                                        GoDefaultPublication.UNIPROTKBKW.zdbID(),
                                        GoDefaultPublication.EC.zdbID(),
                                        GoDefaultPublication.INTERPRO.zdbID()
                                ))
                                .stream()
                                .distinct()
                                //filter? to only allow 'ZDB-TERM-091209-6070','ZDB-TERM-091209-2432','ZDB-TERM-091209-4029'
                                //based on mgte.getGoTerm().getZdbID()
//                                .filter(mgte -> mgte.getGoTerm().getZdbID().equals(GO_TERM_MOLECULAR_FUNCTION_ID)
//                                        || mgte.getGoTerm().getZdbID().equals(GO_TERM_BIOLOGICAL_PROCESS_ID)
//                                        || mgte.getGoTerm().getZdbID().equals(GO_TERM_CELLULAR_COMPONENT_ID))
                                .toList()
                )
        );
    }

    public static List<InterProProteinDTO> fetchExistingInterproDomainRecords() {
        String sql = "select ip_interpro_id, ip_name, ip_type from interpro_protein";
        List queryResults = currentSession().createNativeQuery(sql).list();
        List<InterProProteinDTO> interproDomainRecords = new ArrayList<>();
        for(Object result : queryResults) {
            Object[] row = (Object[]) result;
            String ipInterproId = (String) row[0];
            String ipName = (String) row[1];
            String ipType = (String) row[2];
            interproDomainRecords.add(new InterProProteinDTO(ipInterproId, ipType, ipName));
        }
        return interproDomainRecords;
    }

    public static List<ProteinDTO> fetchExistingProteinRecords() {
        String sql = "select up_uniprot_id, up_length from protein";
        List queryResults = currentSession().createNativeQuery(sql).list();
        List<ProteinDTO> proteinRecords = new ArrayList<>();
        for(Object result : queryResults) {
            Object[] row = (Object[]) result;
            String accession = (String) row[0];
            Integer length = (Integer) row[1];
            proteinRecords.add(new ProteinDTO(accession, length));
        }
        return proteinRecords;
    }

    public static List<MarkerToProteinDTO> fetchExistingMarkerToProteinRecords() {
        String sql = "select mtp_mrkr_zdb_id, mtp_uniprot_id from marker_to_protein";
        List queryResults = currentSession().createNativeQuery(sql).list();
        List<MarkerToProteinDTO> markerToProteinRecords = new ArrayList<>();
        for(Object result : queryResults) {
            Object[] row = (Object[]) result;
            String markerZdbID = (String) row[0];
            String uniprotAccession = (String) row[1];
            markerToProteinRecords.add(new MarkerToProteinDTO(markerZdbID, uniprotAccession));
        }
        return markerToProteinRecords;
    }

    public static List<ProteinToInterproDTO> fetchExistingProteinToInterproRecords() {
        String sql = "select pti_uniprot_id, pti_interpro_id from protein_to_interpro";
        List queryResults = currentSession().createNativeQuery(sql).list();
        List<ProteinToInterproDTO> proteinToInterproRecords = new ArrayList<>();
        for(Object result : queryResults) {
            Object[] row = (Object[]) result;
            String uniprotAccession = (String) row[0];
            String interproId = (String) row[1];
            proteinToInterproRecords.add(new ProteinToInterproDTO(uniprotAccession, interproId));
        }
        return proteinToInterproRecords;
    }

    public static List<PdbDTO> fetchExistingPdbRecords() {
        String sql = "select ptp_uniprot_id, ptp_pdb_id from protein_to_pdb";
        List queryResults = currentSession().createNativeQuery(sql).list();
        List<PdbDTO> pdbRecords = new ArrayList<>();
        for(Object result : queryResults) {
            Object[] row = (Object[]) result;
            String uniprotID = (String) row[0];
            String pdbID = (String) row[1];
            pdbRecords.add(new PdbDTO(uniprotID, pdbID));
        }
        return pdbRecords;
    }


    private Map<String, String> fetchTermOntologyMap() {
        //based on perl logic
        //TODO: convert to more java-like code
        String sql = """
                select distinct term_zdb_id, term_ontology
                 from marker_go_term_evidence, term
                         where term_ont_id like 'GO%'
                           and mrkrgoev_evidence_code = 'IEA'
                           and term_ontology in ('cellular_component', 'molecular_function', 'biological_process')
                           and mrkrgoev_term_zdb_id = term_zdb_id                
                """;
        List queryResults = currentSession().createNativeQuery(sql).list();
        Map<String, String> termOntologyMap = new HashMap<>();
        for(Object result : queryResults) {
            Object[] row = (Object[]) result;
            String termZdbID = (String) row[0];
            String termOntology = (String) row[1];
            termOntologyMap.put(termZdbID, termOntology);
        }
        return termOntologyMap;
    }
//    private void setExternalNotesByUniprotAccession(List<DBLinkExternalNote> dbLinkExternalNoteByPublicationID) {
//        this.externalNotesByUniprotAccession = new HashMap<>();
//        dbLinkExternalNoteByPublicationID.forEach(dbLinkExternalNote -> {
//            DBLinkSlimDTO notesKey = DBLinkSlimDTO.builder()
//                            .accession(dbLinkExternalNote.getDblink().getAccessionNumber())
//                            .dataZdbID(dbLinkExternalNote.getDblink().getDataZdbID())
//                            .build();
//
//            this.externalNotesByUniprotAccession.put(notesKey, DBLinkExternalNoteSlimDTO.from(dbLinkExternalNote));
//        });
//    }

//    public DBLinkExternalNoteSlimDTO getExternalNoteByGeneAndAccession(String geneZdbID, String accessionNumber) {
//        DBLinkSlimDTO notesKey = DBLinkSlimDTO.builder()
//                .accession(accessionNumber)
//                .dataZdbID(geneZdbID)
//                .build();
//
//        if (externalNotesByUniprotAccession == null) {
//            throw new RuntimeException("Initialize error external notes");
//        }
//
//        if (externalNotesByUniprotAccession.containsKey(notesKey)) {
//            return externalNotesByUniprotAccession.get(notesKey);
//        }
//
//        return null;
//    }

    private void createUniprotDbLinksByGeneZdbID() {
        log.info("Creating uniprotDbLinksByGeneZdbID");
        this.uniprotDbLinks.values().stream().flatMap(Collection::stream).forEach(uniprotDbLink -> {
            if(uniprotDbLink.getDataZdbID() != null) {
                if(uniprotDbLinksByGeneZdbID == null) {
                    uniprotDbLinksByGeneZdbID = new HashMap<>();
                }
                if(!uniprotDbLinksByGeneZdbID.containsKey(uniprotDbLink.getDataZdbID())) {
                    uniprotDbLinksByGeneZdbID.put(uniprotDbLink.getDataZdbID(), new ArrayList<>());
                }
                uniprotDbLinksByGeneZdbID.get(uniprotDbLink.getDataZdbID()).add(uniprotDbLink);
            }
        });
        log.info("Finished Creating uniprotDbLinksByGeneZdbID");
    }

    public static Map<String, List<DBLinkSlimDTO>> convertToDTO(Map<String, Collection<MarkerDBLink>> markerDBLinks) {
        Map<String, List<DBLinkSlimDTO>> transformedMap = new HashMap<>();
        for(Map.Entry<String, Collection<MarkerDBLink>> entry : markerDBLinks.entrySet()) {
            String key = entry.getKey();
            ArrayList<DBLinkSlimDTO> dblinks = new ArrayList<>();

            for(MarkerDBLink markerDBLink : entry.getValue()) {
                dblinks.add(DBLinkSlimDTO.builder()
                .accession(markerDBLink.getAccessionNumber())
                .dataZdbID(markerDBLink.getDataZdbID())
                .markerAbbreviation(markerDBLink.getMarker().getAbbreviation())
                .dbName(markerDBLink.getReferenceDatabase().getForeignDB().getDbName().name())
                .publicationIDs( markerDBLink.getPublicationIdsAsList() )
                .build());
            }
            transformedMap.put(key, dblinks);
        }
        return transformedMap;
    }

    public List<DBLinkSlimDTO> getUniprotsByGene(String dataZdbID) {
        if (uniprotDbLinksByGeneZdbID == null) {
            createUniprotDbLinksByGeneZdbID();
        }
        return uniprotDbLinksByGeneZdbID.get(dataZdbID) == null ?
                Collections.emptyList() :
                uniprotDbLinksByGeneZdbID.get(dataZdbID);
    }

    public List<DBLinkSlimDTO> getGenesByUniprot(String dataZdbID) {
        List<DBLinkSlimDTO> results = this.uniprotDbLinks.get(dataZdbID);
        if(results == null) {
            return Collections.emptyList();
        }
        return results;
    }

    public List<String> getGeneZdbIDsByUniprot(String uniprotAccession) {
        List<DBLinkSlimDTO> dblinks = this.uniprotDbLinks.get(uniprotAccession);
        if(dblinks == null) {
            return Collections.emptyList();
        }
        return dblinks.stream().map(DBLinkSlimDTO::getDataZdbID).toList();
    }

    public boolean hasAnyUniprotGeneAssociation(String uniprotAccession, List<String> geneZdbIDs) {
        List<DBLinkSlimDTO> dblinks = this.uniprotDbLinks.get(uniprotAccession);
        if(dblinks == null) {
            return false;
        }
        return dblinks.stream().anyMatch(dbLinkSlimDTO -> geneZdbIDs.contains(dbLinkSlimDTO.getDataZdbID()));
    }

    public boolean hasUniprotGeneAssociation(String uniprotAccession, String geneZdbID) {
        List<DBLinkSlimDTO> dblinks = this.uniprotDbLinks.get(uniprotAccession);
        if(dblinks == null) {
            return false;
        }
        return dblinks.stream().anyMatch(dbLinkSlimDTO -> dbLinkSlimDTO.getDataZdbID().equals(geneZdbID));
    }

    public Map<String, List<DBLinkSlimDTO>> getMapOfDbLinksByAccession(ForeignDB.AvailableName dbName) {
        Map<String, List<DBLinkSlimDTO>> dblinks = switch (dbName) {
            case INTERPRO -> getInterproDbLinks();
            case EC -> getEcDbLinks();
            case PFAM -> getPfamDbLinks();
            case PROSITE -> getPrositeDbLinks();
            case UNIPROTKB -> getUniprotDbLinks();
            default -> Collections.emptyMap();
        };
        if (dblinks == null) {
            return Collections.emptyMap();
        }
        return dblinks;
    }

    public List<DBLinkSlimDTO> getFlattenedDbLinksByDbName(ForeignDB.AvailableName dbName) {
        return getMapOfDbLinksByAccession(dbName).values().stream().flatMap(Collection::stream).toList();
    }

    public DBLinkSlimDTO getDbLinkByGeneAndAccession(ForeignDB.AvailableName dbName, String geneID, String accession) {
        List<DBLinkSlimDTO> dblinks = getMapOfDbLinksByAccession(dbName).get(accession);
        if(dblinks == null) {
            return null;
        }
        return dblinks
                .stream()
                .filter(dbLinkSlimDTO -> dbLinkSlimDTO.getDataZdbID().equals(geneID))
                .findFirst()
                .orElse(null);
    }

//    public Collection<DBLinkExternalNoteSlimDTO> getAllExternalNotes() {
//        Map<DBLinkSlimDTO, DBLinkExternalNoteSlimDTO> map = getExternalNotesByUniprotAccession();
//        return map.values();
//    }

    public void setInterproDbLinksByList(List<DBLinkSlimDTO> dblinks) {
        this.setInterproDbLinks(dblinks.stream().collect(Collectors.groupingBy(DBLinkSlimDTO::getAccession)));
    }

    public void setUniprotDbLinksByList(List<DBLinkSlimDTO> dblinks) {
        this.setUniprotDbLinks(dblinks.stream().collect(Collectors.groupingBy(DBLinkSlimDTO::getAccession)));
    }
    public List<MarkerGoTermEvidenceSlimDTO> getExistingMarkerGoTermEvidenceRecords(ForeignDB.AvailableName dbName) {
        String pubID = getPubIDForMarkerGoTermEvidenceByDB(dbName);

        if (getExistingMarkerGoTermEvidenceRecords() == null) {
            return Collections.emptyList();
        }

        return this.getExistingMarkerGoTermEvidenceRecords()
                .stream()
                .filter(markerGoTermEvidenceSlimDTO -> markerGoTermEvidenceSlimDTO.getPublicationID().equals(pubID))
                .toList();
    }
    public String getPubIDForMarkerGoTermEvidenceByDB(ForeignDB.AvailableName dbName) {
        return switch (dbName) {
            case INTERPRO -> GoDefaultPublication.INTERPRO.zdbID();
            case EC -> GoDefaultPublication.EC.zdbID();
            case UNIPROTKB -> GoDefaultPublication.UNIPROTKBKW.zdbID();
            default -> null;
        };
    }
}
