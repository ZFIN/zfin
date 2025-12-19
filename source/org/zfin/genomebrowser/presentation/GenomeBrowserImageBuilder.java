package org.zfin.genomebrowser.presentation;

import org.zfin.feature.Feature;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.mapping.GenomeLocation;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.sequence.gff.Assembly;

import java.util.Collection;

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

    GenomeBrowserImageBuilder withPadding(int startPadding, int endPadding);

    GenomeBrowserImageBuilder withRelativePadding(double v);

    GenomeBrowserImageBuilder withPadding(int v);

    GenomeBrowserImageBuilder withHeight(int height);

    GenomeBrowserImageBuilder grid(boolean grid);

    String getLandmark();

    GenomeBrowserBuild getGenomeBuild();

    Collection<GenomeBrowserTrack> getTracks();

    String getHighlightLandmark();

    String getHighlightColor();

    boolean isGrid();

    Feature getHighlightFeature();

    GenomeBrowserImage buildForClone(Clone clone);

    Integer getHeight();

    GenomeBrowserImageBuilder setBuild(Assembly assembly);
}
