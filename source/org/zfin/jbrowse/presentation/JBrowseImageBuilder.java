package org.zfin.jbrowse.presentation;

import org.zfin.feature.Feature;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.genomebrowser.presentation.GenomeBrowserImageBuilder;
import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.infrastructure.ZdbID;
import org.zfin.mapping.GenomeLocation;
import org.zfin.marker.Marker;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.zfin.repository.RepositoryFactory.getLinkageRepository;

public class JBrowseImageBuilder implements GenomeBrowserImageBuilder {

    private GenomeBrowserBuild genomeBuild;

    private String landmark;
    private Collection<GenomeBrowserTrack> tracks;
    private String highlightLandmark;
    private String highlightColor;
    private boolean grid;

    private GenomeLocation landmarkLocation;
    private Integer start;
    private Integer end;
    private Integer centeredRange;
    private Integer startPadding;
    private Integer endPadding;
    private Double relativePadding;
    private Marker highlightMarker;
    private Feature highlightFeature;
    private String highlightString;

    public GenomeBrowserImage build() {

        if (highlightMarker != null) {
            highlightLandmark = getHighlightLandmarkByMarkerOrFeature(highlightMarker);
        } else if (highlightFeature != null) {
            highlightLandmark = getHighlightLandmarkByMarkerOrFeature(highlightFeature);
        } else if (highlightString != null) {
            highlightLandmark = highlightString;
        }

        if (genomeBuild == null) {
            genomeBuild = GenomeBrowserBuild.CURRENT;
        }

        // setup range
        if (landmarkLocation != null) {
            start = landmarkLocation.getStart();
            end = landmarkLocation.getEnd();

            if (centeredRange != null) {
                int center = (start + end) / 2;
                start = center - centeredRange / 2;
                end = center + centeredRange / 2;
            } else if (startPadding != null && endPadding != null) {
                start -= startPadding;
                end += endPadding;
            } else if (relativePadding != null) {
                int padding = (int) (relativePadding * (end - start));
                start -= padding;
                end += padding;
            }

            landmark = landmarkLocation.getChromosome() + ":" + start + ".." + end;
        }

        return new JBrowseImage(this);
    }

    private String getHighlightLandmarkByMarkerOrFeature(ZdbID entity) {
        String defaultValue = "";
        if (entity instanceof Marker) {
            defaultValue = ((Marker) entity).getAbbreviation();
        } else if (entity instanceof Feature) {
            defaultValue = ((Feature) entity).getAbbreviation();
        }
        String landmark = defaultValue;

        GenomeLocation location = getGenomeLocation(entity);

        if (location != null) {
            landmark = location.getChromosome() + ":" +
                    location.getStart() + ".." +
                    location.getEnd();
        }
        return landmark;
    }

    private GenomeLocation getGenomeLocation(ZdbID markerOrFeature) {
        List<GenomeLocation> genomeLocations = getLinkageRepository().getGenericGenomeLocation(markerOrFeature);

        Optional<GenomeLocation> first = genomeLocations.stream().filter(
                genomeLocation -> getGenomeBuild().getValue().equals(genomeLocation.getAssembly())
        ).findFirst();

        GenomeLocation result = null;
        if (first.isPresent()) {
            result = first.get();
        }
        return result;
    }

    public GenomeBrowserImageBuilder genomeBuild(GenomeBrowserBuild genomeBuild) {
        this.genomeBuild = genomeBuild;
        return this;
    }

    public GenomeBrowserImageBuilder landmark(String landmark) {
        this.landmark = landmark;
        return this;
    }

    public GenomeBrowserImageBuilder landmark(GenomeLocation landmark) {
        this.landmarkLocation = landmark;
        return this;
    }

    public GenomeBrowserImageBuilder withCenteredRange(int range) {
        this.centeredRange = range;
        return this;
    }

    public GenomeBrowserImageBuilder withPadding(int startPadding, int endPadding) {
        this.startPadding = startPadding;
        this.endPadding = endPadding;
        return this;
    }

    public GenomeBrowserImageBuilder withPadding(double padding) {
        relativePadding = padding;
        return this;
    }

    public GenomeBrowserImageBuilder withPadding(int padding) {
        return withPadding(padding, padding);
    }

    public GenomeBrowserImageBuilder tracks(GenomeBrowserTrack... tracks) {
        return tracks(Arrays.asList(tracks));
    }

    public GenomeBrowserImageBuilder tracks(Collection<GenomeBrowserTrack> tracks) {
        this.tracks = tracks;
        return this;
    }

    public GenomeBrowserImageBuilder highlight(String highlight) {
        highlightString = highlight;
        return this;
    }

    public GenomeBrowserImageBuilder highlight(Marker highlight) {
        highlightMarker = highlight;
        return this;
    }

    public GenomeBrowserBuild getGenomeBuild() {
        return genomeBuild;
    }

    public Collection<GenomeBrowserTrack> getTracks() {
        return tracks;
    }

    public String getHighlightLandmark() {
        return highlightLandmark;
    }

    public String getHighlightColor() {
        return highlightColor;
    }

    public boolean isGrid() {
        return grid;
    }

    public Feature getHighlightFeature() {
        return highlightFeature;
    }

    public GenomeBrowserImageBuilder highlight(Feature highlight) {
        highlightFeature = highlight;
        return this;
    }

    public GenomeBrowserImageBuilder highlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    public GenomeBrowserImageBuilder grid(boolean grid) {
        this.grid = grid;
        return this;
    }

    public String getLandmark() {
        return landmark;
    }
}