
package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-model pairing a marker (EFG or construct) with one of its FPBase fluorescent
 * proteins, plus that protein's emission/excitation lengths and colors.
 *
 * <p>ZFIN-10352 retired the {@code fluorescent_marker} and {@code fpProtein_construct}
 * tables (stale, unmaintained denormalized caches). This was formerly a Hibernate
 * {@code @Entity} mapped to the former; it is now a plain transient POJO built from the
 * live link chain via {@link #of(Marker, FluorescentProtein)} -- an EFG's own
 * {@code fpProtein_efg} links, or a construct's derived from its coding-sequence EFGs.
 * Colors/lengths come straight off the protein.
 */
@Setter
@Getter
public class FluorescentMarkerDTO extends AbstractFluorescence {

    @JsonView(View.API.class)
    private Marker efg;

    @JsonView(View.API.class)
    private FluorescentProtein protein;

    /**
     * All proteins the marker reports, not just this row's. ZFIN-10352: this used to read
     * {@code fluorescentProteinEfgs} directly, which is empty for a construct -- so the
     * construct table's "FPbase Protein" column was always blank. Marker derives the
     * construct case from its coding-sequence EFGs.
     */
    @JsonView(View.API.class)
    public List<FluorescentProtein> getProteins() {
        return new ArrayList<>(efg.getReportedFluorescentProteins());
    }

    @JsonView(View.API.class)
    private Integer excitationLength;

    @JsonView(View.API.class)
    private Integer emissionLength;

    @JsonView(View.API.class)
    private String emissionColor;

    @JsonView(View.API.class)
    private String excitationColor;

    /**
     * Build a transient FluorescentMarkerDTO for a marker↔protein link, copying the
     * protein's lengths and (post-ZFIN-10352-fix) colors. Replaces reading a
     * {@code fluorescent_marker} row.
     */
    public static FluorescentMarkerDTO of(Marker marker, FluorescentProtein protein) {
        FluorescentMarkerDTO fm = new FluorescentMarkerDTO();
        fm.setEfg(marker);
        fm.setProtein(protein);
        fm.setEmissionLength(protein.getEmissionLength());
        fm.setExcitationLength(protein.getExcitationLength());
        fm.setEmissionColor(protein.getEmissionColor());
        fm.setExcitationColor(protein.getExcitationColor());
        return fm;
    }

}
