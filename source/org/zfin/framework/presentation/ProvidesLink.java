package org.zfin.framework.presentation;

/**
 * A method that indicates that we can provide a link method directly on the presentation object.
 */
public interface ProvidesLink {

    String getLink();
    String getLinkWithAttribution();
    String getLinkWithAttributionAndOrderThis() ;
}
