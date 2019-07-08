package org.zfin.publication.presentation;

import org.zfin.curation.presentation.CurationStatusDTO;

import java.util.Date;
import java.util.List;

public class DashboardPublicationBean {

    private String zdbId;
    private String title;
    private String citation;
    private String authors;
    private String abstractText;
    private List<DashboardImageBean> images;
    private String pdfPath;
    private CurationStatusDTO status;
    private Date lastCorrespondenceDate;
    private int numberOfCorrespondences;
    private List<ProcessingTaskBean> processingTasks;
    private boolean canShowImages;

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

    public List<DashboardImageBean> getImages() {
        return images;
    }

    public void setImages(List<DashboardImageBean> images) {
        this.images = images;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public CurationStatusDTO getStatus() {
        return status;
    }

    public void setStatus(CurationStatusDTO status) {
        this.status = status;
    }

    public Date getLastCorrespondenceDate() {
        return lastCorrespondenceDate;
    }

    public void setLastCorrespondenceDate(Date lastCorrespondenceDate) {
        this.lastCorrespondenceDate = lastCorrespondenceDate;
    }

    public int getNumberOfCorrespondences() {
        return numberOfCorrespondences;
    }

    public void setNumberOfCorrespondences(int numberOfCorrespondences) {
        this.numberOfCorrespondences = numberOfCorrespondences;
    }

    public List<ProcessingTaskBean> getProcessingTasks() {
        return processingTasks;
    }

    public void setProcessingTasks(List<ProcessingTaskBean> processingTasks) {
        this.processingTasks = processingTasks;
    }

    public boolean getCanShowImages() {
        return canShowImages;
    }

    public void setCanShowImages(boolean canShowImages) {
        this.canShowImages = canShowImages;
    }
}
