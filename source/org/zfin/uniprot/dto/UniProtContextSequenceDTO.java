package org.zfin.uniprot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UniProtContextSequenceDTO {
    private String accession;
    private String dataZdbID;

    private String markerAbbreviation;
}
