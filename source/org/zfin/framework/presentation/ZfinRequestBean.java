package org.zfin.framework.presentation;

import com.opensymphony.clickstream.Clickstream;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ZfinRequestBean {

    private Clickstream clickstream;
    private String sessionID;

    public ZfinRequestBean(Clickstream clickstream, String sessionID) {
        this.clickstream = clickstream;
        this.sessionID = sessionID;
    }

    public Clickstream getClickstream() {
        return clickstream;
    }

    public String getSessionID() {
        return sessionID;
    }

}
