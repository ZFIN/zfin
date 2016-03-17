package org.zfin.gbrowse.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.mapping.GenomeLocation;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.URLCreator;

import java.util.Arrays;
import java.util.Collection;

public class GBrowseImage {

    private final String landmark;
    private final Collection<GBrowseTrack> tracks;
    private final String highlightFeature;
    private final String highlightColor;
    private final boolean grid;

    private String imageUrlBase;
    private String imageUrl;
    private String linkUrlBase;
    private String linkUrl;

    private GenomeBuild build;

    public static GBrowseImageBuilder builder() {
        return new GBrowseImageBuilder();
    }

    private GBrowseImage(GBrowseImageBuilder builder) {
        this.landmark = builder.landmark;
        this.tracks = builder.tracks;
        this.highlightFeature = builder.highlightLandmark;
        this.highlightColor = builder.highlightColor;
        this.grid = builder.grid;

        this.build = builder.genomeBuild;
        this.linkUrlBase = this.build.getPath();
        this.imageUrlBase= this.build.getImagePath();
    }

    public String getImageUrl() {
        if (imageUrl == null) {
            URLCreator url = new URLCreator(imageUrlBase);

            if (StringUtils.isNotBlank(landmark)) {
                url.addNamevaluePair("name", landmark);
            }

            if (CollectionUtils.isNotEmpty(tracks)) {
                url.addNamevaluePair("type", StringUtils.join(tracks, " "));
            }

            String highlight = getHighlightString();
            if (StringUtils.isNotBlank(highlight)) {
                url.addNamevaluePair("h_feat", highlight);
            }

            url.addNamevaluePair("grid", grid ? "1" : "0");

            imageUrl = "/" + url.getURL();
        }
        return imageUrl;
    }

    public String getLinkUrl() {
        if (linkUrl == null) {
            URLCreator url = new URLCreator(linkUrlBase);

            if (StringUtils.isNotBlank(landmark)) {
                url.addNamevaluePair("name", landmark);
            }

            String highlight = getHighlightString();
            if (StringUtils.isNotBlank(highlight)) {
                url.addNamevaluePair("h_feat", highlight);
            }

            linkUrl = "/" + url.getURL();
        }
        return linkUrl;
    }

    public String getLandmark() {
        return landmark;
    }

    public String getBuild() {
        return build.getValue();
    }

    private String getHighlightString() {
        String urlHighlight = "";
        if (StringUtils.isNotBlank(highlightFeature)) {
            urlHighlight = highlightFeature;
            if (StringUtils.isNotBlank(highlightColor)) {
                urlHighlight += "@" + highlightColor;
            }
        }
        // gbrowse wants the highlight name to be lowercase for some reason!
        return urlHighlight.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GBrowseImage image = (GBrowseImage) o;

        if (!getImageUrl().equals(image.getImageUrl())) return false;
        if (!getLinkUrl().equals(image.getLinkUrl())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getImageUrl() != null ? getImageUrl().hashCode() : 0;
        result = 31 * result + (getLinkUrl() != null ? getLinkUrl().hashCode() : 0);
        return result;
    }

    public static class GBrowseImageBuilder {

        private GenomeBuild genomeBuild;

        private String landmark;
        private Collection<GBrowseTrack> tracks;
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

        public GBrowseImage build() {

            if (highlightMarker != null) {
                highlightLandmark = highlightMarker.getAbbreviation();
            } else if (highlightFeature != null) {
                highlightLandmark = highlightFeature.getAbbreviation();
            } else if (highlightString != null) {
                highlightLandmark = highlightString;
            }

            if (genomeBuild == null) {
                genomeBuild = GenomeBuild.CURRENT;
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

            return new GBrowseImage(this);
        }

        public GenomeBuild getGenomeBuild() {
            return genomeBuild;
        }

        public void setGenomeBuild(GenomeBuild genomeBuild) {
            this.genomeBuild = genomeBuild;
        }

        public GBrowseImageBuilder landmark(String landmark) {
            this.landmark = landmark;
            return this;
        }

        public GBrowseImageBuilder landmark(GenomeLocation landmark) {
            this.landmarkLocation = landmark;
            return this;
        }

        public GBrowseImageBuilder withCenteredRange(int range) {
            this.centeredRange = range;
            return this;
        }

        public GBrowseImageBuilder withPadding(int startPadding, int endPadding) {
            this.startPadding = startPadding;
            this.endPadding = endPadding;
            return this;
        }

        public GBrowseImageBuilder withPadding(double padding) {
            relativePadding = padding;
            return this;
        }

        public GBrowseImageBuilder withPadding(int padding) {
            return withPadding(padding, padding);
        }

        public GBrowseImageBuilder tracks(GBrowseTrack... tracks) {
            return tracks(Arrays.asList(tracks));
        }

        public GBrowseImageBuilder tracks(Collection<GBrowseTrack> tracks) {
            this.tracks = tracks;
            return this;
        }

        public GBrowseImageBuilder highlight(String highlight) {
            highlightString = highlight;
            return this;
        }

        public GBrowseImageBuilder highlight(Marker highlight) {
            highlightMarker = highlight;
            return this;
        }

        public GBrowseImageBuilder highlight(Feature highlight) {
            highlightFeature = highlight;
            return this;
        }

        public GBrowseImageBuilder highlightColor(String highlightColor) {
            this.highlightColor = highlightColor;
            return this;
        }

        public GBrowseImageBuilder grid(boolean grid) {
            this.grid = grid;
            return this;
        }

    }

    public enum GenomeBuild {
        ZV9("Zv9", ZfinPropertiesEnum.GBROWSE_ZV9_PATH_FROM_ROOT.toString(), ZfinPropertiesEnum.GBROWSE_ZV9_IMG_PATH_FROM_ROOT.toString()),
        CURRENT("GRCz10", ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT.toString(), ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT.toString());

        private final String value;
        private final String path;
        private final String imagePath;

        GenomeBuild(String value, String path, String imagePath) {
            this.value = value;
            this.path = path;
            this.imagePath = imagePath;
        }

        public String getValue() { return value; }

        public String getPath() {
            return path;
        }

        public String getImagePath() {
            return imagePath;
        }
    }


}
