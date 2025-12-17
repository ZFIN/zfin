package org.zfin.jbrowse.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.genomebrowser.presentation.GenomeBrowserImageBuilder;
import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.infrastructure.ZdbID;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.gff.Assembly;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    private Integer height;

    @Override
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

    public GenomeBrowserImage buildForClone(Clone clone) {
        List<MarkerGenomeLocation> cloneLocations = RepositoryFactory.getLinkageRepository().getGenomeLocationWithCoordinates(clone);
        if (CollectionUtils.isEmpty(cloneLocations)) {
            return null;
        }

        // jbrowse image
        return this.setLandmarkByGenomeLocation(cloneLocations.get(0))
                .highlight(clone)
                .tracks(new GenomeBrowserTrack[]{GenomeBrowserTrack.COMPLETE_CLONES, GenomeBrowserTrack.GENES, GenomeBrowserTrack.TRANSCRIPTS})
                .withRelativePadding(0.2)
                .build();
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

        return genomeLocations.stream().filter(
            genomeLocation -> (getGenomeBuild() == null ? GenomeBrowserBuild.CURRENT : getGenomeBuild())
                               .getValue().equals(genomeLocation.getAssembly())
        )
        .findFirst()
        .orElse(null);
    }

    @Override
    public GenomeBrowserImageBuilder genomeBuild(GenomeBrowserBuild genomeBuild) {
        this.genomeBuild = genomeBuild;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder landmark(String landmark) {
        this.landmark = landmark;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder setLandmarkByGenomeLocation(GenomeLocation landmark) {
        this.landmarkLocation = landmark;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder withCenteredRange(int range) {
        this.centeredRange = range;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder withPadding(int startPadding, int endPadding) {
        this.startPadding = startPadding;
        this.endPadding = endPadding;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder withRelativePadding(double padding) {
        relativePadding = padding;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder withPadding(int padding) {
        return withPadding(padding, padding);
    }

    @Override
    public GenomeBrowserImageBuilder withHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder tracks(GenomeBrowserTrack... tracks) {
        return tracks(Arrays.asList(tracks));
    }

    private GenomeBrowserImageBuilder tracks(Collection<GenomeBrowserTrack> tracks) {
        this.tracks = tracks;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder highlight(String highlight) {
        highlightString = highlight;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder highlight(Marker highlight) {
        highlightMarker = highlight;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder highlight(Feature highlight) {
        highlightFeature = highlight;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder highlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    @Override
    public GenomeBrowserImageBuilder grid(boolean grid) {
        this.grid = grid;
        return this;
    }

    @Override
    public String getLandmark() {
        return landmark;
    }

    @Override
    public GenomeBrowserBuild getGenomeBuild() {
        return genomeBuild;
    }

    @Override
    public Collection<GenomeBrowserTrack> getTracks() {
        return tracks;
    }

    @Override
    public String getHighlightLandmark() {
        return highlightLandmark;
    }

    @Override
    public String getHighlightColor() {
        return highlightColor;
    }

    @Override
    public boolean isGrid() {
        return grid;
    }

    @Override
    public Feature getHighlightFeature() {
        return highlightFeature;
    }

    @Override
    public Integer getHeight() {
        return height;
    }

    @Override
    public GenomeBrowserImageBuilder setBuild(Assembly assembly) {
        return null;
    }

}