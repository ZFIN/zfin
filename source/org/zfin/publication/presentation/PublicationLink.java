package org.zfin.publication.presentation;

import org.zfin.framework.presentation.ProvidesLink;

/**
 */
public class PublicationLink implements ProvidesLink{

    private String publicationZdbId ;
    private String linkContent ;
    private String type ;

    @Override
    public String getLink() {
        return PublicationPresentation.getLink(publicationZdbId,linkContent);
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }

    public String getPublicationZdbId() {
        return publicationZdbId;
    }

    public void setPublicationZdbId(String publicationZdbId) {
        this.publicationZdbId = publicationZdbId;
    }

    public String getLinkContent() {
        return linkContent;
    }

    public void setLinkContent(String linkContent) {
        this.linkContent = linkContent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
