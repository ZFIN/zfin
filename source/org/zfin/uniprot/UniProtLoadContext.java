package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
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
public class UniProtLoadContext {

    private Map<String, List<DBLinkSlimDTO>> uniprotDbLinks;
    private Map<String, List<DBLinkSlimDTO>> refseqDbLinks;

    public static UniProtLoadContext createFromDBConnection() {
        UniProtLoadContext uniprotLoadContext = new UniProtLoadContext();

        ReferenceDatabase uniprotRefDB = getSequenceRepository().getReferenceDatabase(UNIPROTKB, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
        uniprotLoadContext.setUniprotDbLinks( convertToDTO(getSequenceRepository().getMarkerDBLinks(uniprotRefDB)) );

        ReferenceDatabase refseqRefDB = getSequenceRepository().getReferenceDatabase(REFSEQ, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
        ReferenceDatabase refseqRNARefDB = getSequenceRepository().getReferenceDatabase(REFSEQ, RNA, SEQUENCE, ZEBRAFISH);

        Map<String, Collection<MarkerDBLink>> markerDBLinks = getSequenceRepository().getMarkerDBLinks(refseqRNARefDB, refseqRefDB);
        uniprotLoadContext.setRefseqDbLinks( convertToDTO(markerDBLinks));

        return uniprotLoadContext;
    }

    private static Map<String, List<DBLinkSlimDTO>> convertToDTO(Map<String, Collection<MarkerDBLink>> markerDBLinks) {
        Map<String, List<DBLinkSlimDTO>> transformedMap = new HashMap<>();
        for(Map.Entry<String, Collection<MarkerDBLink>> entry : markerDBLinks.entrySet()) {
            String key = entry.getKey();
            ArrayList<DBLinkSlimDTO> sequenceDTOs = new ArrayList<>();

            for(MarkerDBLink markerDBLink : entry.getValue()) {
                DBLinkSlimDTO sequenceDTO = new DBLinkSlimDTO();
                sequenceDTO.setAccession(markerDBLink.getAccessionNumber());
                sequenceDTO.setDataZdbID(markerDBLink.getDataZdbID());
                sequenceDTO.setMarkerAbbreviation(markerDBLink.getMarker().getAbbreviation());
                sequenceDTO.setDbName(markerDBLink.getReferenceDatabase().getForeignDB().getDbName().name());
                sequenceDTO.setPublicationIDs( markerDBLink.getPublicationIdsAsList() );
                sequenceDTOs.add(sequenceDTO);
            }
            transformedMap.put(key, sequenceDTOs);
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
}
