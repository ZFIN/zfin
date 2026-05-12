package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.marker.Marker;
import org.zfin.zirc.entity.Gene;

/**
 * Wire format for one per-mutation gene row. {@code mutatedGeneZdbId} is the
 * Marker's ZDB ID (FK to public.marker); {@code mutatedGeneAbbreviation} is a
 * read-only echo of the resolved Marker for display in the editor — the
 * server populates it on the way out, the client doesn't send it.
 */
@Getter
@Setter
@NoArgsConstructor
public class GeneDTO {

    private Long id;
    private Integer sortOrder;
    private String mutatedGeneZdbId;
    private String mutatedGeneAbbreviation;
    private String linkageGroup;
    private String genbankGenomicDna;
    private String genbankCdna;

    public static GeneDTO from(Gene g) {
        GeneDTO dto = new GeneDTO();
        dto.setId(g.getId());
        dto.setSortOrder(g.getSortOrder());
        Marker m = g.getMutatedGene();
        if (m != null) {
            dto.setMutatedGeneZdbId(m.getZdbID());
            dto.setMutatedGeneAbbreviation(m.getAbbreviation());
        }
        dto.setLinkageGroup(g.getLinkageGroup());
        dto.setGenbankGenomicDna(g.getGenbankGenomicDna());
        dto.setGenbankCdna(g.getGenbankCdna());
        return dto;
    }
}
