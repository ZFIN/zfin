package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.curation.presentation.CurationStatusDTO;
import org.zfin.expression.Image;
import org.zfin.framework.api.View;

import java.util.Date;
import java.util.List;

public class DashboardPublicationBean {

    @JsonView(View.API.class) private String zdbId;
    @JsonView(View.API.class) private String title;
    @JsonView(View.API.class) private String citation;
    @JsonView(View.API.class) private String authors;
    @JsonView(View.API.class) private String abstractText;
    @JsonView(View.API.class) private List<Image> images;
    @JsonView(View.API.class) private String pdfPath;
    @JsonView(View.API.class) private CurationStatusDTO status;
    @JsonView(View.API.class) private Date lastCorrespondenceDate;
    @JsonView(View.API.class) private int numberOfCorrespondences;
    @JsonView(View.API.class) private List<ProcessingTaskBean> processingTasks;
    @JsonView(View.API.class) private boolean canShowImages;

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

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
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
