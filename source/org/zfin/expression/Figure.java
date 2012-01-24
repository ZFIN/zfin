package org.zfin.expression;

import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.Set;

/**
 * Figure domain business object. It is a figure referenced in a publication.
 */
public abstract class Figure implements Serializable, Comparable<Figure> {

    public enum Type {
        FIGURE("figure"),
        TOD("text only");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String zdbID;
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
        if (label != null && label.contains("Table")) {
            return label.substring(label.indexOf("Table"));
        }
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

    public abstract Type getType();

    public boolean equals(Object otherFigure) {
        if (!(otherFigure instanceof Figure))
            return false;

        Figure figure = (Figure) otherFigure;
        return getZdbID().equals(figure.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

    public int compareTo(Figure compFig) {
        return orderingLabel.compareTo(compFig.getOrderingLabel());
    }

    public boolean isImgless() {
        if (images == null || images.isEmpty())
            return true;
        else
            return false;
    }

    public String getMediumSizedImg() {
        if (isImgless())
          return "";

        return "http://trunk.zfin.org/imageLoadUp/250/" + getImages().iterator().next().getZdbID() + ".jpg";
    }

    public Image getImg() {
        if (isImgless())
          return null;

        return getImages().iterator().next();
    }

    public int getCaptionWordCount() {
        if (caption == null)
            return 0;
        return caption.length();
    }

    public String getConciseCaption() {
        if (getCaptionWordCount() > 780) {
           return caption.substring(0,780);
        }
        return caption;
    }

    public int getConciseCaptionWordCount() {
        if (getConciseCaption() == null)
            return 0;
        return getConciseCaption().length();
    }
}
