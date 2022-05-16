package org.zfin.genomebrowser.presentation;

import org.zfin.feature.Feature;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.mapping.GenomeLocation;
import org.zfin.marker.Marker;

public interface GenomeBrowserImageBuilder {
    GenomeBrowserImage build();

    GenomeBrowserImageBuilder genomeBuild(GenomeBrowserBuild genomeBuild);

    GenomeBrowserImageBuilder highlight(String highlight);

    GenomeBrowserImageBuilder highlight(Marker trackingGene);

    GenomeBrowserImageBuilder highlight(Feature feature);

    GenomeBrowserImageBuilder highlightColor(String pink);

    GenomeBrowserImageBuilder setLandmarkByGenomeLocation(GenomeLocation landmark);

    GenomeBrowserImageBuilder landmark(String s);

    GenomeBrowserImageBuilder tracks(GenomeBrowserTrack[] genomeBrowserTracks);

    GenomeBrowserImageBuilder withCenteredRange(int i);

    GenomeBrowserImageBuilder withRelativePadding(double v);

    GenomeBrowserImageBuilder withPadding(int v);

}
