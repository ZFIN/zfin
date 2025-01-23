package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.*;

import static org.zfin.Species.Type.ZEBRAFISH;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.ForeignDB.AvailableName.UNIPROTKB;
import static org.zfin.sequence.ForeignDB.AvailableName.REFSEQ;
import static org.zfin.sequence.ForeignDBDataType.DataType.POLYPEPTIDE;
import static org.zfin.sequence.ForeignDBDataType.DataType.RNA;
import static org.zfin.sequence.ForeignDBDataType.SuperType.SEQUENCE;
import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

/**
 * This class is meant to represent the context in which a UniProt load is being performed.
 * Specifically, it is the contents of the database that gets referenced during the load.
 */
@Getter
@Setter
@Log4j2
public class UniProtLoadContext {

    //indexed by uniprot accession
    private Map<String, List<DBLinkSlimDTO>> uniprotDbLinks;

    //indexed by refseq accession
    private Map<String, List<DBLinkSlimDTO>> refseqDbLinks;

    //indexed by gene zdb id
    private Map<String, List<DBLinkSlimDTO>> uniprotDbLinksByGeneID;

    public static UniProtLoadContext createFromDBConnection() {
        UniProtLoadContext uniprotLoadContext = new UniProtLoadContext();

        ReferenceDatabase uniprotRefDB = getSequenceRepository().getReferenceDatabase(UNIPROTKB, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);

        log.debug("Getting uniprot db links (markerdblinks) from database");
        Map<String, Collection<MarkerDBLink>> tempDbLinks = getSequenceRepository().getMarkerDBLinks(uniprotRefDB);
        log.debug("Setting uniprot db links in context object");
        uniprotLoadContext.setUniprotDbLinks( convertToDTO(tempDbLinks) );

        ReferenceDatabase refseqRefDB = getSequenceRepository().getReferenceDatabase(REFSEQ, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
        ReferenceDatabase refseqRNARefDB = getSequenceRepository().getReferenceDatabase(REFSEQ, RNA, SEQUENCE, ZEBRAFISH);

        log.debug("Getting refseq links");
        Map<String, Collection<MarkerDBLink>> markerDBLinks = getSequenceRepository().getMarkerDBLinks(refseqRNARefDB, refseqRefDB);
        log.debug("Setting marker db links in context object");
        uniprotLoadContext.setRefseqDbLinks( convertToDTO(markerDBLinks));

        log.debug("Set up mappings");
        uniprotLoadContext.setUniprotDbLinksByGeneID( uniprotLoadContext.getInitialMapOfUniprotDbLinksByGeneID() );
        return uniprotLoadContext;
    }

    private static Map<String, List<DBLinkSlimDTO>> convertToDTO(Map<String, Collection<MarkerDBLink>> markerDBLinks) {
        Map<String, List<DBLinkSlimDTO>> transformedMap = new HashMap<>();
        for(Map.Entry<String, Collection<MarkerDBLink>> entry : markerDBLinks.entrySet()) {
            String key = entry.getKey();
            ArrayList<DBLinkSlimDTO> dblinks = new ArrayList<>();

            for(MarkerDBLink markerDBLink : entry.getValue()) {
                dblinks.add(
                        DBLinkSlimDTO.builder()
                        .accession(markerDBLink.getAccessionNumber())
                        .dataZdbID(markerDBLink.getDataZdbID())
                        .markerAbbreviation(markerDBLink.getMarker().getAbbreviation())
                        .dbName(markerDBLink.getReferenceDatabase().getForeignDB().getDbName().name())
                        .publicationIDs( markerDBLink.getPublicationIdsAsList())
                        .build());
            }
            transformedMap.put(key, dblinks);
        }
        return transformedMap;
    }

    public DBLinkSlimDTO getDBLinkByUniprotAndGene(String accession, String gene) {
        List<DBLinkSlimDTO> dblinks = this.getUniprotDbLinks().get(accession);
        if (dblinks == null) {
            return null;
        }
        for (DBLinkSlimDTO dbl : dblinks) {
            if (dbl.getDataZdbID().equals(gene)) {
                return dbl;
            }
        }
        return null;
    }

    public boolean hasExistingUniprotForGene(String accession, String gene) {
        return getDBLinkByUniprotAndGene(accession, gene) != null;
    }

    /**
     * Does our current database state (context) have a uniprot entry for this gene? In addition, does it have
     * an attribution that is not the result of automated curation of uniprot database links?
     *
     * @param accession
     * @param gene
     * @return
     */
    public boolean hasExistingUniprotWithNonLoadAttributions(String accession, String gene) {
        if (hasExistingUniprotForGene(accession, gene)) {
            DBLinkSlimDTO existingDBLink = getDBLinkByUniprotAndGene(accession, gene);
            if (existingDBLink.getPublicationIDs().size() == 1 &&
                    existingDBLink.getPublicationIDs().get(0).equals(AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS) ) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private Map<String, List<DBLinkSlimDTO>> getInitialMapOfUniprotDbLinksByGeneID() {
        Map<String, List<DBLinkSlimDTO>> allGenesWithDBLinks = new HashMap<>();
        for (List<DBLinkSlimDTO> dblinks : this.getUniprotDbLinks().values()) {
            for (DBLinkSlimDTO dbl : dblinks) {
                if (!allGenesWithDBLinks.containsKey(dbl.getDataZdbID())) {
                    allGenesWithDBLinks.put(dbl.getDataZdbID(), new ArrayList<>());
                }
                allGenesWithDBLinks.get(dbl.getDataZdbID()).add(dbl);
            }
        }
        return allGenesWithDBLinks;
    }
}
