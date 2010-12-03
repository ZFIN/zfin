package org.zfin.publication;

import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.mutant.Phenotype;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

/**
 * ToDo:
 */
public class Publication implements Comparable<Publication>, Serializable {

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
    private String doi;
    private GregorianCalendar publicationDate;
    private GregorianCalendar closeDate;
    private Journal journal;
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<Phenotype> phenotypes;
    private Set<Figure> figures;

    private boolean deletable;

    //todo: make type into a proper enum, with tests
    //for now I only need one value, so I'll just be quick and dirty
    public static final String CURATION = "Curation";
    public static final String UNPUBLISHED = "Unpublished";

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

    public Set<Phenotype> getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(Set<Phenotype> phenotypes) {
        this.phenotypes = phenotypes;
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

    public GregorianCalendar getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(GregorianCalendar closeDate) {
        this.closeDate = closeDate;
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

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
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
        if (journal != null) {
            sb.append(journal.getName());
            sb.append(". ");
        }
        sb.append(volume);
        if (pages != null)
            sb.append(":" + pages);

        return sb.toString();

    }

    public int getYear() {
        return publicationDate.get(Calendar.YEAR);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("Publication");
        sb.append(newline);
        sb.append("Title: " + title);
        sb.append(newline);
        sb.append("Authors: " + authors);
        sb.append(newline);
        sb.append("ZDB ID: " + zdbID);
        sb.append(newline);
        sb.append("accession: " + accessionNumber);
        sb.append(newline);
        sb.append("doi: " + doi);
        return sb.toString();
    }

    public int compareTo(Publication anotherPublication) {
        if (publicationDate == null)
            return -1;
        if (anotherPublication.getPublicationDate() == null)
            return +1;
        if (publicationDate.get(GregorianCalendar.YEAR) > anotherPublication.getPublicationDate().get(GregorianCalendar.YEAR))
            return -1;
        if (publicationDate.get(GregorianCalendar.YEAR) < anotherPublication.getPublicationDate().get(GregorianCalendar.YEAR))
            return +1;

        // in case the 2 publications have the same publication dates,
        // compare the authors
        return authors.compareToIgnoreCase(anotherPublication.getAuthors());
    }

    public boolean isUnpublished() {
        return (type.equalsIgnoreCase(Publication.CURATION) || type.equalsIgnoreCase(Publication.UNPUBLISHED));
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean equals(Object otherPublication) {
        if (!(otherPublication instanceof Publication))
            return false;

        Publication publication = (Publication) otherPublication;
        return getZdbID().equals(publication.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

}
