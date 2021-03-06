package org.zfin.figure.presentation;

import java.util.Set;

public class FigurePresentationBean {

    private String zdbId;
    private String pubZdbId;
    private String label;
    private String caption;
    private int numExpressionStatements;
    private int numPhenotypeStatements;
    private Set<ImagePresentationBean> images;

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public String getPubZdbId() {
        return pubZdbId;
    }

    public void setPubZdbId(String pubZdbId) {
        this.pubZdbId = pubZdbId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Set<ImagePresentationBean> getImages() {
        return images;
    }

    public int getNumExpressionStatements() {
        return numExpressionStatements;
    }

    public void setNumExpressionStatements(int numExpressionStatements) {
        this.numExpressionStatements = numExpressionStatements;
    }

    public int getNumPhenotypeStatements() {
        return numPhenotypeStatements;
    }

    public void setNumPhenotypeStatements(int numPhenotypeStatements) {
        this.numPhenotypeStatements = numPhenotypeStatements;
    }

    public void setImages(Set<ImagePresentationBean> images) {
        this.images = images;
    }
}
