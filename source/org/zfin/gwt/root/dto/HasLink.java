package org.zfin.gwt.root.dto;

/**
 * For DTOs that have links.
 */
public interface HasLink {
    String getLink() ;
    void setLink(String link) ;
    String getName() ;
    void setName(String name) ;
    String getPublicationZdbID() ;
    void setPublicationZdbID(String publication) ;
    boolean isEditable() ;

    <U extends HasLink> U deepCopy() ;
}
