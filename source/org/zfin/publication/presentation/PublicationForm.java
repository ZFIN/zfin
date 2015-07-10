package org.zfin.publication.presentation;


import org.zfin.publication.Journal;
import org.zfin.publication.Publication;

import java.util.GregorianCalendar;

public class PublicationForm {

    private String zdbID;
    private String title;
    private Publication.Status status;
    private String pubMedID;
    private String doi;
    private String authors;
    private GregorianCalendar date;
    private Journal journal;
    private String volume;
    private String pages;
    private Publication.Type type;
    private String keywords;
    private String abstractText;
    private String notes;

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getPubMedID() {
        return pubMedID;
    }

    public void setPubMedID(String pubMedID) {
        this.pubMedID = pubMedID;
    }

    public Publication.Status getStatus() {
        return status;
    }

    public void setStatus(Publication.Status status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Publication.Type getType() {
        return type;
    }

    public void setType(Publication.Type type) {
        this.type = type;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


}
