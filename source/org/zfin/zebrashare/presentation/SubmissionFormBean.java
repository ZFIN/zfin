package org.zfin.zebrashare.presentation;

import org.springframework.web.multipart.MultipartFile;
import org.zfin.profile.Lab;

public class SubmissionFormBean {

    private String authors;
    private String title;
    private String abstractText;

    private String submitterName;
    private String submitterEmail;
    private String labZdbId;
    private String[] editors;

    private MultipartFile dataFile;
    private MultipartFile[] imageFiles;
    private String[] captions;

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public String getLabZdbId() {
        return labZdbId;
    }

    public void setLabZdbId(String labZdbId) {
        this.labZdbId = labZdbId;
    }

    public String[] getEditors() {
        return editors;
    }

    public void setEditors(String[] editors) {
        this.editors = editors;
    }

    public MultipartFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(MultipartFile dataFile) {
        this.dataFile = dataFile;
    }

    public MultipartFile[] getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(MultipartFile[] imageFiles) {
        this.imageFiles = imageFiles;
    }

    public String[] getCaptions() {
        return captions;
    }

    public void setCaptions(String[] captions) {
        this.captions = captions;
    }
}
