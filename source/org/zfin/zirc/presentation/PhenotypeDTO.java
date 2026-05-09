package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.Phenotype;

/**
 * Wire format for one per-mutation phenotype row. {@code segregation} and
 * {@code type} are postgres {@code text[]} arrays of small enum-like values
 * (e.g. {@code "mendelian_recessive"}, {@code "zygotic"}). The form spec
 * eventually wants checkbox groups; for now the React side accepts any
 * strings and the server stores them verbatim.
 */
@Getter
@Setter
@NoArgsConstructor
public class PhenotypeDTO {

    private Long id;
    private Integer sortOrder;
    private String description;
    private Integer hoursPostFertilization;
    private String stage;
    private Boolean zfinImagePermission;
    private Double nonMendelianPercentage;
    private String[] segregation;
    private String[] type;

    public static PhenotypeDTO from(Phenotype p) {
        PhenotypeDTO dto = new PhenotypeDTO();
        dto.setId(p.getId());
        dto.setSortOrder(p.getSortOrder());
        dto.setDescription(p.getDescription());
        dto.setHoursPostFertilization(p.getHoursPostFertilization());
        dto.setStage(p.getStage());
        dto.setZfinImagePermission(p.getZfinImagePermission());
        dto.setNonMendelianPercentage(p.getNonMendelianPercentage());
        dto.setSegregation(p.getSegregation());
        dto.setType(p.getType());
        return dto;
    }
}
