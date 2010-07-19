package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.HTML;

/**
*/
class ReferenceComposite extends HTML {

    private String pubZdbID ;

    public ReferenceComposite(String publicationZdbID) {
        this.pubZdbID = publicationZdbID ;
        setHTML("<a href=\"/cgi-bin/webdriver/?MIval=aa-pubview2.apg&OID="+pubZdbID+"\">"+pubZdbID+"</a>");
    }
}
