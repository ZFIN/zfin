package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.Lesion;

/**
 * Wire format for one per-mutation molecular-lesion row. Field set matches
 * the xlsx matrix (Field summary of Molecular nature of the mutation.xlsx):
 * the lesionType picks which subset is meaningful, the rest are null.
 */
@Getter
@Setter
@NoArgsConstructor
public class LesionDTO {

    private Long id;
    private Integer sortOrder;
    private String lesionType;
    private Integer lesionSizeBp;
    private Integer insertionSizeBp;
    private String nucleotideChange;
    private String deletedSequence;
    private String insertedSequence;
    private String transgeneSequence;
    private String locationInline;
    private String fivePrimeFlank;
    private String threePrimeFlank;
    private Boolean hasLargeVariant;
    private String mutatedAminoAcids;
    private String mutatedAminoAcidsHgvs;
    private String additionalInfo;

    public static LesionDTO from(Lesion l) {
        LesionDTO dto = new LesionDTO();
        dto.setId(l.getId());
        dto.setSortOrder(l.getSortOrder());
        dto.setLesionType(l.getLesionType());
        dto.setLesionSizeBp(l.getLesionSizeBp());
        dto.setInsertionSizeBp(l.getInsertionSizeBp());
        dto.setNucleotideChange(l.getNucleotideChange());
        dto.setDeletedSequence(l.getDeletedSequence());
        dto.setInsertedSequence(l.getInsertedSequence());
        dto.setTransgeneSequence(l.getTransgeneSequence());
        dto.setLocationInline(l.getLocationInline());
        dto.setFivePrimeFlank(l.getFivePrimeFlank());
        dto.setThreePrimeFlank(l.getThreePrimeFlank());
        dto.setHasLargeVariant(l.getHasLargeVariant());
        dto.setMutatedAminoAcids(l.getMutatedAminoAcids());
        dto.setMutatedAminoAcidsHgvs(l.getMutatedAminoAcidsHgvs());
        dto.setAdditionalInfo(l.getAdditionalInfo());
        return dto;
    }
}
