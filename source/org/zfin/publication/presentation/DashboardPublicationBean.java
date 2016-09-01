package org.zfin.publication.presentation;

import java.util.Calendar;
import java.util.List;

public class DashboardPublicationBean {

    private String zdbId;
    private String title;
    private String citation;
    private String authors;
    private String abstractText;
    private List<String> figurePaths;
    private Calendar lastUpdate;
    private String pdfPath;

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public List<String> getFigurePaths() {
        return figurePaths;
    }

    public void setFigurePaths(List<String> figurePaths) {
        this.figurePaths = figurePaths;
    }

    public Calendar getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Calendar lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
}
