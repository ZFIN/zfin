package org.zfin.gbrowse.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.gbrowse.GBrowseService;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.URLCreator;

import java.util.*;

public class GBrowseImage {

    private final String landmark;
    private final Collection<GBrowseTrack> tracks;
    private final String highlightFeature;
    private final String highlightColor;
    private final boolean grid;

    private String imageUrl;
    private String linkUrl;

    public static GBrowseImageBuilder builder() {
        return new GBrowseImageBuilder();
    }

    private GBrowseImage(GBrowseImageBuilder builder) {
        this.landmark = builder.landmark;
        this.tracks = builder.tracks;
        this.highlightFeature = builder.highlightLandmark;
        this.highlightColor = builder.highlightColor;
        this.grid = builder.grid;
    }

    public String getImageUrl() {
        if (imageUrl == null) {
            URLCreator url = new URLCreator(ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT.toString());

            if (StringUtils.isNotBlank(landmark)) {
                url.addNamevaluePair("name", landmark);
            }

            if (CollectionUtils.isNotEmpty(tracks)) {
                url.addNamevaluePair("type", StringUtils.join(tracks, " "));
            }

            if (StringUtils.isNotBlank(highlightFeature)) {
                String urlHighlight = highlightFeature;
                if (StringUtils.isNotBlank(highlightColor)) {
                    urlHighlight += "@" + highlightColor;
                }
                url.addNamevaluePair("h_feat", urlHighlight.toLowerCase());
            }

            url.addNamevaluePair("grid", grid ? "1" : "0");

            imageUrl = "/" + url.getURL();
        }
        return imageUrl;
    }

    public String getLinkUrl() {
        if (linkUrl == null) {
            URLCreator url = new URLCreator(ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT.toString());

            if (StringUtils.isNotBlank(landmark)) {
                url.addNamevaluePair("name", landmark);
            }

            linkUrl = "/" + url.getURL();
        }
        return linkUrl;
    }

    public String getLandmark() {
        return landmark;
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

        private String landmark;
        private Collection<GBrowseTrack> tracks;
        private String highlightLandmark;
        private String highlightColor;
        private boolean grid;

        private Marker landmarkMarker;
        private Feature landmarkFeature;
        private String landmarkString;
        private Integer start;
        private Integer end;
        private Integer centeredRange;
        private Integer startPadding;
        private Integer endPadding;
        private Double relativePadding;
        private boolean useDefaultTracks;
        private Marker highlightMarker;
        private Feature highlightFeature;
        private String highlightString;
        private boolean useDefaultHighlight;
        private Integer chromosome;

        private final LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository();

        public GBrowseImage build() {

            if (useDefaultHighlight) {
                highlightMarker = landmarkMarker;
                highlightFeature = landmarkFeature;
                highlightString = landmarkString;
            }

            List<? extends GenomeLocation> landmarkLocations = null;
            if (landmarkMarker != null) {
                landmarkLocations = linkageRepository.getGenomeLocation(landmarkMarker, GenomeLocation.Source.ZFIN);
            } else if (landmarkFeature != null) {
                landmarkLocations = linkageRepository.getGenomeLocation(landmarkFeature, GenomeLocation.Source.ZFIN);
            }

            if (CollectionUtils.isEmpty(landmarkLocations)) {
                landmarkMarker = highlightMarker;
                landmarkFeature = highlightFeature;
            }

            // setup landmark -- if a marker or feature was given, look up its genome location. if it has genome
            // location info, use it to set the start and end, otherwise just use the abbreviation. if a string was
            // given, just use that string.
            if (landmarkMarker != null) {
                List<MarkerGenomeLocation> locations = linkageRepository.getGenomeLocation(landmarkMarker, GenomeLocation.Source.ZFIN);
                if (CollectionUtils.isNotEmpty(locations)) {
                    processLocations(locations);
                } else {
                    landmark = landmarkMarker.getAbbreviation();
                }
            } else if (landmarkFeature != null) {
                List<FeatureGenomeLocation> locations = linkageRepository.getGenomeLocation(landmarkFeature, GenomeLocation.Source.ZFIN);
                if (CollectionUtils.isNotEmpty(locations)) {
                    processLocations(locations);
                } else {
                    landmark = landmarkFeature.getAbbreviation();
                }
            } else if (landmarkString != null) {
                landmark = landmarkString;
            }

            // setup highlight -- if start and end were set (via landmark) try looking up the highlight marker/feature
            // genome location because we may need to adjust the start and end.
            if (highlightMarker != null) {
                if (start != null && end != null) {
                    processHighlightLocations(linkageRepository.getGenomeLocation(highlightMarker, GenomeLocation.Source.ZFIN));
                }
                highlightLandmark = highlightMarker.getAbbreviation();
            } else if (highlightFeature != null) {
                if (start != null && end != null) {
                    processHighlightLocations(linkageRepository.getGenomeLocation(highlightFeature, GenomeLocation.Source.ZFIN));
                }
                highlightLandmark = highlightFeature.getAbbreviation();
            } else if (highlightString != null) {
                highlightLandmark = highlightString;
            }

            // setup range
            if (start != null && end != null) {
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

                landmark += ":" + start + ".." + end;
            }

            if (useDefaultTracks && landmarkMarker != null) {
                tracks = Arrays.asList(GBrowseService.getGBrowseTracks(landmarkMarker));
            }

            return new GBrowseImage(this);
        }

        public GBrowseImageBuilder landmark(String landmark) {
            landmarkString = landmark;
            return this;
        }

        public GBrowseImageBuilder landmark(Marker landmark) {
            landmarkMarker = landmark;
            return this;
        }

        public GBrowseImageBuilder landmark(Feature landmark) {
            landmarkFeature = landmark;
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

        public GBrowseImageBuilder withDefaultTracks() {
            useDefaultTracks = true;
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

        public GBrowseImageBuilder highlight() {
            useDefaultHighlight = true;
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

        private void processLocations(List<? extends GenomeLocation> locations) {
            Collections.sort(locations);
            landmark = locations.get(0).getChromosome();
            chromosome = Integer.parseInt(landmark, 10);
            List<Integer> starts = new ArrayList<>();
            List<Integer> ends = new ArrayList<>();
            for (GenomeLocation location : locations) {
                starts.add(location.getStart());
                ends.add(location.getEnd());
            }
            start = Collections.min(starts);
            end = Collections.max(ends);
        }

        private void processHighlightLocations(List<? extends GenomeLocation> locations) {
            Collections.sort(locations);
            for (GenomeLocation location : locations) {
                if (chromosome != null && chromosome != Integer.parseInt(location.getChromosome(), 10)) {
                    return;
                }
                start = Math.min(start, location.getStart());
                end = Math.max(end, location.getEnd());
            }
        }

    }
}
