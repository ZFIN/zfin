package org.zfin.expression.presentation;

/**
 */
public class PublicationExpressionBean {

    private int numFigures ;
    private String publicationZdbID ;
    private String miniAuth ;
    private String probeFeatureAbbrev;
    private String probeFeatureZdbId ;
    private int numImages;

    public int getNumFigures() {
        return numFigures;
    }

    public void setNumFigures(int numFigures) {
        this.numFigures = numFigures;
    }

    public String getPublicationZdbID() {
        return publicationZdbID;
    }

    public void setPublicationZdbID(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID;
    }

    public String getMiniAuth() {
        return miniAuth;
    }

    public void setMiniAuth(String miniAuth) {
        this.miniAuth = miniAuth;
    }

    public String getProbeFeatureAbbrev() {
        return probeFeatureAbbrev;
    }

    public void setProbeFeatureAbbrev(String probeFeatureAbbrev) {
        this.probeFeatureAbbrev = probeFeatureAbbrev;
    }

    public int getNumImages() {
        return numImages;
    }

    public void setNumImages(int numImages) {
        this.numImages = numImages;
    }

    public String getProbeFeatureZdbId() {
        return probeFeatureZdbId;
    }

    public void setProbeFeatureZdbId(String probeFeatureZdbId) {
        this.probeFeatureZdbId = probeFeatureZdbId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicationExpressionBean that = (PublicationExpressionBean) o;

        if (publicationZdbID != null ? !publicationZdbID.equals(that.publicationZdbID) : that.publicationZdbID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return publicationZdbID != null ? publicationZdbID.hashCode() : 0;
    }
}
