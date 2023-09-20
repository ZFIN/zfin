package org.zfin.uniprot.dto;

import org.biojavax.CrossRef;

public class UniProtCrossReferenceDTO {
    public String dbName;
    public String accession;

    public static UniProtCrossReferenceDTO from(CrossRef crossRef) {
        UniProtCrossReferenceDTO dto = new UniProtCrossReferenceDTO();
        dto.dbName = crossRef.getDbname();
        dto.accession = crossRef.getAccession();
        return dto;
    }
}
