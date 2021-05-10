package org.zfin.marker.presentation;

import org.zfin.publication.Publication;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkerReferenceBean that = (MarkerReferenceBean) o;
        return Objects.equals(zdbID, that.zdbID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zdbID);
    }
}
