package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.HTML;

/**
*/
class ReferenceComposite extends HTML {

    private String pubZdbID ;

    public ReferenceComposite(String publicationZdbID) {
        this.pubZdbID = publicationZdbID ;
        setHTML("<a href=\"/"+pubZdbID+"\">"+pubZdbID+"</a>");
    }
}
