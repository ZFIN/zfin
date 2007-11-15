package org.zfin.publication;

import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Set;
import java.util.GregorianCalendar;

/**
 * ToDo:
 */
public class Publication {

    private String zdbID;
    private String title;
    private String authors;
    private String shortAuthorList;
    private String abstractText;
    private String volume;
    private String pages;
    private String type;
    private String accessionNumber;
    private String fileName;
    private String doi ;
    private GregorianCalendar publicationDate;
    private Journal journal;
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<Figure> figures;

    //todo: make type into a proper enum, with tests
    //for now I only need one value, so I'll just be quick and dirty
    public static final String CURATION = "Curation";

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

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getShortAuthorList() {
        return shortAuthorList;
    }

    public void setShortAuthorList(String shortAuthorList) {
        this.shortAuthorList = shortAuthorList;
    }

    public GregorianCalendar getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(GregorianCalendar publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDoi()
    {
        return doi;
    }
    
    public void setDoi(String doi)
    {
        this.doi = doi;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public Set<Figure> getFigures() {
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        return null;
        //return pr.getFiguresByPublication(zdbID);
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public String getCitation() {
        StringBuilder sb = new StringBuilder();
        sb.append(authors);
        sb.append(" (");
        sb.append(publicationDate.get(GregorianCalendar.YEAR));
        sb.append(") ");
        sb.append(title);
        sb.append(". ");
        if (journal != null){
            sb.append(journal.getName());
            sb.append( ". ");
        }
        sb.append(volume);
        if (pages != null)
            sb.append(":" + pages);

        return sb.toString();

    }




    public String toString() {
        StringBuilder sb = new StringBuilder("Publication");
        sb.append("\r");
        sb.append("Title: " + title);
        sb.append("\r");
        sb.append("Authors: " + authors);
        sb.append("\r");
        sb.append("ZDB ID: " + zdbID);
        return sb.toString();
    }
    
    
}
