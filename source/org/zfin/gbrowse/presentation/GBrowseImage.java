package org.zfin.gbrowse.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserType;
import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.util.URLCreator;

import java.util.Collection;

public class GBrowseImage implements GenomeBrowserImage {

    private final String landmark;
    private final Collection<GBrowseTrack> tracks;
    private final String highlightFeature;
    private final String highlightColor;
    private final boolean grid;

    private String imageUrlBase;
    private String imageUrl;
    private String linkUrlBase;
    private String linkUrl;

    private GenomeBrowserBuild build;
    private GenomeBrowserType type = GenomeBrowserType.GBROWSE;

    public GBrowseImage(GBrowseImageBuilder builder) {
        this.landmark = builder.getLandmark();

        this.tracks = GBrowseTrack.fromGenomeBrowserTracks(builder.getTracks());

        this.highlightFeature = builder.getHighlightLandmark();
        this.highlightColor = builder.getHighlightColor();
        this.grid = builder.isGrid();

        this.build = builder.getGenomeBuild();
        this.linkUrlBase = this.build.getPath();
        this.imageUrlBase= this.build.getImagePath();
    }

    @Override
    public String getReactComponentId() {
            return "GbrowseImage";
    }

    @Override
    public String getImageUrl() {
        if (imageUrl == null) {
            URLCreator url = new URLCreator(imageUrlBase);

            if (StringUtils.isNotBlank(landmark)) {
                url.addNameValuePair("name", landmark);
            }

            if (CollectionUtils.isNotEmpty(tracks)) {
                url.addNameValuePair("type", StringUtils.join(tracks, " "));
            }

            String highlight = getHighlightString();
            if (StringUtils.isNotBlank(highlight)) {
                url.addNameValuePair("h_feat", highlight);
            }

            url.addNameValuePair("grid", grid ? "1" : "0");

            imageUrl = "/" + url.getURL();
        }
        return imageUrl;
    }

    @Override
    public String getLinkUrl() {
        if (linkUrl == null) {
            URLCreator url = new URLCreator(linkUrlBase);

            if (StringUtils.isNotBlank(landmark)) {
                url.addNameValuePair("name", landmark);
            }

            String highlight = getHighlightString();
            if (StringUtils.isNotBlank(highlight)) {
                url.addNameValuePair("h_feat", highlight);
            }

            linkUrl = "/" + url.getURL();
        }
        return linkUrl;
    }

    @Override
    public String getLandmark() {
        return landmark;
    }

    @Override
    public GenomeBrowserType getType() {
        return type;
    }

    @Override
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
}