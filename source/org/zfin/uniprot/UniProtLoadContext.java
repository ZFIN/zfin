package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.SequenceList;
import org.zfin.uniprot.dto.UniProtContextSequenceDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.zfin.Species.Type.ZEBRAFISH;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.ForeignDB.AvailableName.UNIPROTKB;
import static org.zfin.sequence.ForeignDB.AvailableName.REFSEQ;
import static org.zfin.sequence.ForeignDBDataType.DataType.POLYPEPTIDE;
import static org.zfin.sequence.ForeignDBDataType.DataType.RNA;
import static org.zfin.sequence.ForeignDBDataType.SuperType.SEQUENCE;

/**
 * This class is meant to represent the context in which a UniProt load is being performed.
 * Specifically, it is the contents of the database that gets referenced during the load.
 */
@Getter
@Setter
public class UniProtLoadContext {

    private Map<String, List<UniProtContextSequenceDTO>> uniprotDbLinks;
    private Map<String, List<UniProtContextSequenceDTO>> refseqDbLinks;

    public static UniProtLoadContext createFromDBConnection() {
        UniProtLoadContext uniprotLoadContext = new UniProtLoadContext();

        ReferenceDatabase uniprotRefDB = getSequenceRepository().getReferenceDatabase(UNIPROTKB, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
        uniprotLoadContext.setUniprotDbLinks( convertToDTO(getSequenceRepository().getMarkerDBLinks(uniprotRefDB)) );

        UniProtContextSequenceDTO sequenceDTO = new UniProtContextSequenceDTO();

        ReferenceDatabase refseqRefDB = getSequenceRepository().getReferenceDatabase(REFSEQ, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
        ReferenceDatabase refseqRNARefDB = getSequenceRepository().getReferenceDatabase(REFSEQ, RNA, SEQUENCE, ZEBRAFISH);

        System.out.println("refseqRefDB: " + refseqRefDB.getZdbID());
        System.out.println("refseqRNARefDB: " + refseqRNARefDB.getZdbID());


        Map<String, Collection<MarkerDBLink>> markerDBLinks = getSequenceRepository().getMarkerDBLinks(refseqRNARefDB, refseqRefDB);
        uniprotLoadContext.setRefseqDbLinks( convertToDTO(markerDBLinks));

        return uniprotLoadContext;
    }

    private static Map<String, List<UniProtContextSequenceDTO>> convertToDTO(Map<String, Collection<MarkerDBLink>> markerDBLinks) {
        Map<String, List<UniProtContextSequenceDTO>> transformedMap = new HashMap<String, List<UniProtContextSequenceDTO>>();
        for(Map.Entry<String, Collection<MarkerDBLink>> entry : markerDBLinks.entrySet()) {
            String key = entry.getKey();
            ArrayList<UniProtContextSequenceDTO> sequenceDTOs = new ArrayList<UniProtContextSequenceDTO>();

            for(MarkerDBLink markerDBLink : entry.getValue()) {
                UniProtContextSequenceDTO sequenceDTO = new UniProtContextSequenceDTO();
                sequenceDTO.setAccession(markerDBLink.getAccessionNumber());
                sequenceDTO.setDataZdbID(markerDBLink.getDataZdbID());
                sequenceDTO.setMarkerAbbreviation(markerDBLink.getMarker().getAbbreviation());
                sequenceDTOs.add(sequenceDTO);
            }
            transformedMap.put(key, sequenceDTOs);
        }
        return transformedMap;
    }
}
