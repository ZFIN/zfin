package org.zfin.uniprot.dto;

import org.zfin.uniprot.diff.RichSequenceDiff;

import java.util.List;

public class RichSequenceDiffDTO {
    public String accession;
    public List<UniProtCrossReferenceDTO> addedCrossRefs;
    public List<UniProtCrossReferenceDTO> removedCrossRefs;
    public List<String> addedKeywords;
    public List<String> removedKeywords;

    public RichSequenceDTO oldSequence;
    public RichSequenceDTO newSequence;

    public static RichSequenceDiffDTO from(RichSequenceDiff richSequenceDiff) {
        RichSequenceDiffDTO dto = new RichSequenceDiffDTO();
        dto.accession = richSequenceDiff.getAccession();
        dto.addedCrossRefs = richSequenceDiff.getAddedCrossRefs().stream().map(UniProtCrossReferenceDTO::from).toList();
        dto.removedCrossRefs = richSequenceDiff.getRemovedCrossRefs().stream().map(UniProtCrossReferenceDTO::from).toList();
        dto.addedKeywords = richSequenceDiff.getAddedKeywords().stream().map(kw -> kw.getValue()).toList();
        dto.removedKeywords = richSequenceDiff.getRemovedKeywords().stream().map(kw -> kw.getValue()).toList();
        dto.oldSequence = RichSequenceDTO.from(richSequenceDiff.getOldSequence());
        dto.newSequence = RichSequenceDTO.from(richSequenceDiff.getNewSequence());
        return dto;
    }
}
