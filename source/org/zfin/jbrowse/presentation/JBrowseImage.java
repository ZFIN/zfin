package org.zfin.jbrowse.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.genomebrowser.GenomeBrowserType;
import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.URLCreator;

import java.util.Collection;

public class JBrowseImage implements GenomeBrowserImage {

    private final String landmark;
    private final Collection<GenomeBrowserTrack> tracks;
    private final String highlightFeature;
    private final String highlightColor;
    private final boolean grid;

    private String imageUrl;
    private String linkUrlBase;
    private String linkUrl;

    private static final int DEFAULT_HEIGHT = 400;
    private Integer height = DEFAULT_HEIGHT;

    private GenomeBrowserBuild build;
    private GenomeBrowserType type = GenomeBrowserType.JBROWSE;

    public JBrowseImage(JBrowseImageBuilder builder) {
        this.landmark = builder.getLandmark();
        this.tracks = builder.getTracks();
        this.highlightFeature = builder.getHighlightLandmark();
        this.highlightColor = builder.getHighlightColor();
        this.grid = builder.isGrid();
        this.height = builder.getHeight();
        this.build = builder.getGenomeBuild();
        this.linkUrlBase = this.build.getJBrowsePath();
    }

    @Override
    public String getReactComponentId() {
        return "JbrowseImage";
    }

    @Override
    public String getImageUrl() {
        if (imageUrl == null) {
            URLCreator url = new URLCreator("");

            //Remove UI elements from JBrowse view
            url.addNameValuePair("nav", "0");
            url.addNameValuePair("overview","0");
            url.addNameValuePair("tracklist","0");

            if (StringUtils.isNotBlank(landmark)) {
                url.addNameValuePair("loc", landmark);
            }

            if (CollectionUtils.isNotEmpty(tracks)) {
                url.addNameValuePair("tracks", StringUtils.join(tracks, ","));
            }

            if (build != null) {
                url.addNameValuePair("data", "data/" + build.getValue());
            }

            String highlight = getHighlightString();
            if (StringUtils.isNotBlank(highlight)) {
                //Disable highlighting by marker for now
                //TODO: uncomment when highlighting by marker is supported in jbrowse project.
//                url.addNameValuePair("highlight", highlight);
            }

            //url.addNameValuePair("grid", grid ? "1" : "0");

            //getURL converts the spaces in the track names to plus signs - this changes them back
            String pathSuffix = url.getURL().replaceAll("\\+","%20");
            String baseUrl = calculateBaseUrl();
            imageUrl = baseUrl + pathSuffix;
        }
        return imageUrl;
    }

    public static String calculateBaseUrl() {
        String url = ZfinPropertiesEnum.JBROWSE_BASE_URL.value();

        boolean useProxy = "true".equals(ZfinPropertiesEnum.JBROWSE_USE_LOCAL_PROXY.value());
        if (useProxy) {
            url = ZfinPropertiesEnum.JBROWSE_PROXY_BASE_URL.value();
        }

        if (!url.endsWith("/")) {
            url += "/";
        }

        return url;
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

            if (CollectionUtils.isNotEmpty(tracks)) {
                url.addNameValuePair("tracks", StringUtils.join(tracks, ","));
            }

            //for some reason, jbrowse doesn't like the "+" encoding
            linkUrl = "/" + url.getURL().replace("+", "%20");
        }
        return linkUrl;
    }

    @Override
    public String getLandmark() {
        return landmark;
    }

    @Override
    public String getChromosome(){
        if(landmark == null)
            return "";
        return landmark.substring(0, landmark.indexOf(":"));
    }

    @Override
    public GenomeBrowserType getType() {
        return type;
    }

    @Override
    public Integer getHeight() {
        if (height == null) {
            return DEFAULT_HEIGHT;
        }
        return height;
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

        JBrowseImage image = (JBrowseImage) o;

        return getLinkUrl().equals(image.getLinkUrl()) &&
                getImageUrl().equals(image.getImageUrl());
    }

    @Override
    public int hashCode() {
        int result = getImageUrl() != null ? getImageUrl().hashCode() : 0;
        result = 31 * result + (getLinkUrl() != null ? getLinkUrl().hashCode() : 0);
        return result;
    }


}
