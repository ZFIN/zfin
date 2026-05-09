package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.Lesion;

/**
 * Wire format for one per-mutation molecular-lesion row.
 */
@Getter
@Setter
@NoArgsConstructor
public class LesionDTO {

    private Long id;
    private Integer sortOrder;
    private String lesionType;
    private Integer indexDeletionPos;
    private Integer indexInsertionSize;
    private String deletedBasePairs;
    private String insertedBasePairs;
    private String wtGenomicSequence;
    private String mutatedAminoAcids;
    private String additionalInfo;

    public static LesionDTO from(Lesion l) {
        LesionDTO dto = new LesionDTO();
        dto.setId(l.getId());
        dto.setSortOrder(l.getSortOrder());
        dto.setLesionType(l.getLesionType());
        dto.setIndexDeletionPos(l.getIndexDeletionPos());
        dto.setIndexInsertionSize(l.getIndexInsertionSize());
        dto.setDeletedBasePairs(l.getDeletedBasePairs());
        dto.setInsertedBasePairs(l.getInsertedBasePairs());
        dto.setWtGenomicSequence(l.getWtGenomicSequence());
        dto.setMutatedAminoAcids(l.getMutatedAminoAcids());
        dto.setAdditionalInfo(l.getAdditionalInfo());
        return dto;
    }
}
