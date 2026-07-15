
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
 * <p>ZFIN-10352 retired the {@code fluorescent_marker} table (a stale, unmaintained
 * denormalized cache). This was formerly a Hibernate {@code @Entity} mapped to that
 * table; it is now a plain transient POJO built from the live link chain
 * ({@code fpProtein_efg}/{@code fpProtein_construct} → {@code fluorescent_protein}) via
 * {@link #of(Marker, FluorescentProtein)}. Colors/lengths come straight off the protein.
 */
@Setter
@Getter
public class FluorescentMarker extends AbstractFluorescence {

    @JsonView(View.API.class)
    private Marker efg;

    @JsonView(View.API.class)
    private FluorescentProtein protein;

    @JsonView(View.API.class)
    public List<FluorescentProtein> getProteins() {
        return new ArrayList<>(efg.getFluorescentProteinEfgs());
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
     * Build a transient FluorescentMarker for a marker↔protein link, copying the
     * protein's lengths and (post-ZFIN-10352-fix) colors. Replaces reading a
     * {@code fluorescent_marker} row.
     */
    public static FluorescentMarker of(Marker marker, FluorescentProtein protein) {
        FluorescentMarker fm = new FluorescentMarker();
        fm.setEfg(marker);
        fm.setProtein(protein);
        fm.setEmissionLength(protein.getEmissionLength());
        fm.setExcitationLength(protein.getExcitationLength());
        fm.setEmissionColor(protein.getEmissionColor());
        fm.setExcitationColor(protein.getExcitationColor());
        return fm;
    }

}
