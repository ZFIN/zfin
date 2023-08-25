package org.zfin.uniprot.dto;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.DatFileWriter;

public class RichSequenceDTO {
    public String accession;
    public String rawData;

    public static RichSequenceDTO from(RichSequenceAdapter richSequence) {
        RichSequenceDTO dto = new RichSequenceDTO();
        dto.accession = richSequence.getAccession();
        dto.rawData = DatFileWriter.sequenceToString(richSequence);
        return dto;
    }

}
