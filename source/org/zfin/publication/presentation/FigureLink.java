package org.zfin.publication.presentation;

import org.zfin.framework.presentation.ProvidesLink;

/**
 */
public class FigureLink implements ProvidesLink{

    private String figureZdbId;
    private String linkContent ;
    private String linkValue ;

    @Override
    public String getLink() {
        if(linkValue==null){
            return PublicationPresentation.getLink(figureZdbId,linkContent);
        }
        else {
            return linkValue;
        }
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }

    public String getFigureZdbId() {
        return figureZdbId;
    }

    public void setFigureZdbId(String figureZdbId) {
        this.figureZdbId = figureZdbId;
    }

    public String getLinkContent() {
        return linkContent;
    }

    public void setLinkContent(String linkContent) {
        this.linkContent = linkContent;
    }

    public String getLinkValue() {
        return linkValue;
    }

    public void setLinkValue(String linkValue) {
        this.linkValue = linkValue;
    }
}
