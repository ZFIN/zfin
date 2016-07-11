package org.zfin.figure.presentation;

import org.zfin.expression.Image;

public class FigureGalleryImagePresentation {

    private Image image;
    private Object titleLinkEntity;
    private Object imageLinkEntity;
    private FigureExpressionSummary figureExpressionSummary;
    private FigurePhenotypeSummary figurePhenotypeSummary;
    private String details;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Object getTitleLinkEntity() {
        return titleLinkEntity;
    }

    public void setTitleLinkEntity(Object titleLinkEntity) {
        this.titleLinkEntity = titleLinkEntity;
    }

    public Object getImageLinkEntity() {
        return imageLinkEntity;
    }

    public void setImageLinkEntity(Object imageLinkEntity) {
        this.imageLinkEntity = imageLinkEntity;
    }

    public FigureExpressionSummary getFigureExpressionSummary() {
        return figureExpressionSummary;
    }

    public void setFigureExpressionSummary(FigureExpressionSummary figureExpressionSummary) {
        this.figureExpressionSummary = figureExpressionSummary;
    }

    public FigurePhenotypeSummary getFigurePhenotypeSummary() {
        return figurePhenotypeSummary;
    }

    public void setFigurePhenotypeSummary(FigurePhenotypeSummary figurePhenotypeSummary) {
        this.figurePhenotypeSummary = figurePhenotypeSummary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}
