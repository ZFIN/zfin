package org.zfin.uniprot.dto;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.UniProtTools;

public class RichSequenceDTO {
    public String accession;
    public String rawData;

    public static RichSequenceDTO from(RichSequence richSequence) {
        RichSequenceDTO dto = new RichSequenceDTO();
        dto.accession = richSequence.getAccession();
        dto.rawData = UniProtTools.sequenceToString(richSequence);
        return dto;
    }

}
