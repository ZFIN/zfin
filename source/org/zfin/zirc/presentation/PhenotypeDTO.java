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
 *
 * <p>hpfStart / hpfEnd: timing as either a single point ({@code hpfEnd}
 * null) or a range. Storage is always integer hpf; the client's hpf/dpf
 * unit toggle is purely a display convenience.
 *
 * <p>stage: server-managed cache derived from hpfStart via the STAGE
 * table. Inbound DTO values are ignored; the saved value is whatever
 * the lookup returns.
 */
@Getter
@Setter
@NoArgsConstructor
public class PhenotypeDTO {

    private Long id;
    private Integer sortOrder;
    private String description;
    private Integer hpfStart;
    private Integer hpfEnd;
    private String stage;
    private Boolean zfinImagePermission;
    private Boolean zircImagePermission;
    private Double nonMendelianPercentage;
    private String nonMendelianComment;
    private String[] segregation;
    private String[] type;

    public static PhenotypeDTO from(Phenotype p) {
        PhenotypeDTO dto = new PhenotypeDTO();
        dto.setId(p.getId());
        dto.setSortOrder(p.getSortOrder());
        dto.setDescription(p.getDescription());
        dto.setHpfStart(p.getHpfStart());
        dto.setHpfEnd(p.getHpfEnd());
        dto.setStage(p.getStage());
        dto.setZfinImagePermission(p.getZfinImagePermission());
        dto.setZircImagePermission(p.getZircImagePermission());
        dto.setNonMendelianPercentage(p.getNonMendelianPercentage());
        dto.setNonMendelianComment(p.getNonMendelianComment());
        dto.setSegregation(p.getSegregation());
        dto.setType(p.getType());
        return dto;
    }
}
