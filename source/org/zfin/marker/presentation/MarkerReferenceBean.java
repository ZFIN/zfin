package org.zfin.marker.presentation;

import org.zfin.publication.Publication;

public class MarkerReferenceBean {

    private String zdbID;
    private String title;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static MarkerReferenceBean convert(Publication publication) {
        MarkerReferenceBean bean = new MarkerReferenceBean();
        bean.setZdbID(publication.getZdbID());
        bean.setTitle(publication.getTitle());
        return bean;
    }
}
