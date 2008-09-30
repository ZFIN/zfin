package org.zfin.expression;

import org.zfin.publication.Publication;

import java.util.Set;
import java.io.Serializable;

/**
 * Figure domain business object. It is a figure referenced in a publication.
 */
public class Figure implements Serializable {

    private String zdbID;
    // Todo should be an image object: private    String sourceID;
    private String caption;
    private String comments;
    private String label;
    private String orderingLabel;
    private Set<ExpressionResult> expressionResults;
    private Set<Image> images;
    private Publication publication;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOrderingLabel() {
        return orderingLabel;
    }

    public void setOrderingLabel(String orderingLabel) {
        this.orderingLabel = orderingLabel;
    }

    public Set<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(Set<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }


    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }
}
