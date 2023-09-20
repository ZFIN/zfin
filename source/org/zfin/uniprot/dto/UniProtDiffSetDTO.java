package org.zfin.uniprot.dto;

import org.zfin.uniprot.diff.UniProtDiffSet;
import org.zfin.uniprot.diff.UniProtDiffSetSummary;

import java.util.List;

public class UniProtDiffSetDTO {
    public UniProtDiffSetSummary summary;
    public List<RichSequenceDTO> addedSequences;
    public List<RichSequenceDTO> removedSequences;
    public List<RichSequenceDiffDTO> changedSequences;

    public static UniProtDiffSetDTO from(UniProtDiffSet diffSet) {
        UniProtDiffSetDTO dto = new UniProtDiffSetDTO();
        dto.summary = diffSet.getSummary();
        dto.addedSequences = diffSet.getAddedSequences().stream().map(RichSequenceDTO::from).toList();
        dto.removedSequences = diffSet.getRemovedSequences().stream().map(RichSequenceDTO::from).toList();
        dto.changedSequences = diffSet.getChangedSequences().stream().map(RichSequenceDiffDTO::from).toList();
        return dto;
    }
}
