package org.zfin.publication;

import org.zfin.curation.Correspondence;
import org.zfin.curation.PublicationNote;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.Person;

import java.io.Serializable;
import java.util.*;

/**
 * ToDo:
 */
public class Publication implements Comparable<Publication>, Serializable, EntityZdbID {

    private String zdbID;
    private String title;
    private String authors;
    private String shortAuthorList;
    private String abstractText;
    private String volume;
    private String pages;
    private Type type;
    private String accessionNumber;
    private String doi;
    private String acknowledgment;
    private Status status;
    private String keywords;
    private String errataAndNotes;
    private GregorianCalendar publicationDate;
    private GregorianCalendar closeDate;
    private GregorianCalendar entryDate;
    private Journal journal;
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<Figure> figures;
    //yes, should be authors, but that conflicts with the string field
    private Set<Person> people;
    private SortedSet<MeshHeading> meshHeadings;
    private Set<PublicationNote> notes;
    private Set<Correspondence> correspondences;
    private Set<PublicationDbXref> dbXrefs;
    private SortedSet<PublicationFile> files;

    private Set<CorrespondenceSentMessage> sentMessages;
    private Set<CorrespondenceReceivedMessage> receivedMessages;
    private Date lastSentEmailDate;

    private boolean deletable;
    private boolean indexed;
    private boolean canShowImages;
    private GregorianCalendar indexedDate;

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

    public GregorianCalendar getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(GregorianCalendar closeDate) {
        this.closeDate = closeDate;
    }

    public GregorianCalendar getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(GregorianCalendar entryDate) {
        this.entryDate = entryDate;
    }

    public GregorianCalendar getIndexedDate() {
        return indexedDate;
    }

    public void setIndexedDate(GregorianCalendar indexedDate) {
        this.indexedDate = indexedDate;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getFileName() {
        for (PublicationFile file : files) {
            if (file.getType().getName() == PublicationFileType.Name.ORIGINAL_ARTICLE) {
                return file.getFileName();
            }
        }
        return null;
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
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public String getAcknowledgment() {
        return acknowledgment;
    }

    public void setAcknowledgment(String acknowledgment) {
        this.acknowledgment = acknowledgment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<Person> getPeople() {
        return people;
    }

    public void setPeople(Set<Person> people) {
        this.people = people;
    }

    public String getErrataAndNotes() {
        return errataAndNotes;
    }

    public void setErrataAndNotes(String errataAndNotes) {
        this.errataAndNotes = errataAndNotes;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public SortedSet<MeshHeading> getMeshHeadings() {
        return meshHeadings;
    }

    public void setMeshHeadings(SortedSet<MeshHeading> meshHeadings) {
        this.meshHeadings = meshHeadings;
    }

    public Set<PublicationNote> getNotes() {
        return notes;
    }

    public void setNotes(Set<PublicationNote> notes) {
        this.notes = notes;
    }

    public Set<Correspondence> getCorrespondences() {
        return correspondences;
    }

    public void setCorrespondences(Set<Correspondence> correspondences) {
        this.correspondences = correspondences;
    }

    public Set<PublicationDbXref> getDbXrefs() {
        return dbXrefs;
    }

    public void setDbXrefs(Set<PublicationDbXref> dbXrefs) {
        this.dbXrefs = dbXrefs;
    }

    public SortedSet<PublicationFile> getFiles() {
        return files;
    }

    public void setFiles(SortedSet<PublicationFile> files) {
        this.files = files;
    }

    public Set<CorrespondenceSentMessage> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(Set<CorrespondenceSentMessage> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public Set<CorrespondenceReceivedMessage> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(Set<CorrespondenceReceivedMessage> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public Date getLastSentEmailDate() {
        return lastSentEmailDate;
    }

    public void setLastSentEmailDate(Date lastSentEmailDate) {
        this.lastSentEmailDate = lastSentEmailDate;
    }

    public String getCitation() {
        StringBuilder sb = new StringBuilder();
        sb.append(authors);
        if (publicationDate != null) {
            sb.append(" (");
            sb.append(getYear());
            sb.append(") ");
        }
        sb.append(title);
        sb.append(". ");
        sb.append(getJournalAndPages());
        return sb.toString();
    }

    public String getJournalAndPages() {
        StringBuilder sb = new StringBuilder();
        if (journal != null) {
            sb.append(journal.getName());
            sb.append(". ");
        }
        sb.append(volume);
        if (pages != null) {
            sb.append(":").append(pages);
        }
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
        if (publicationDate == null) {
            return -1;
        }
        if (anotherPublication.getPublicationDate() == null) {
            return +1;
        }
        if (publicationDate.get(GregorianCalendar.YEAR) > anotherPublication.getPublicationDate().get(GregorianCalendar.YEAR)) {
            return -1;
        }
        if (publicationDate.get(GregorianCalendar.YEAR) < anotherPublication.getPublicationDate().get(GregorianCalendar.YEAR)) {
            return +1;
        }

        // in case the 2 publications have the same publication dates,
        // compare the authors
        int authorComparison = authors.compareToIgnoreCase(anotherPublication.getAuthors());
        if (authorComparison != 0) {
            return authorComparison;
        }
        // sort by zdbID at last criteria
        return zdbID.compareTo(anotherPublication.getZdbID());
    }

    public boolean isUnpublished() {
        return (type == Publication.Type.CURATION || type == Publication.Type.UNPUBLISHED);
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isCanShowImages() {
        return canShowImages;
    }

    public void setCanShowImages(boolean imageShown) {
        this.canShowImages = imageShown;
    }

    public boolean equals(Object otherPublication) {
        if (!(otherPublication instanceof Publication)) {
            return false;
        }

        Publication publication = (Publication) otherPublication;
        return getZdbID().equals(publication.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

    public boolean isNoFigure() {
        if (figures == null || figures.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isForMutantDataFromOldLiterature() {
        if (zdbID.equalsIgnoreCase("ZDB-PUB-060503-2")) {
            return true;
        }
        return false;
    }

    public boolean isOpen() {
        if (closeDate == null) {
            return true;
        }
        return false;
    }

    @Override
    public String getAbbreviation() {
        return shortAuthorList;
    }

    @Override
    public String getAbbreviationOrder() {
        return shortAuthorList;
    }

    @Override
    public String getEntityType() {
        return "Publication";
    }

    @Override
    public String getEntityName() {
        return title;
    }

    public enum Type {
        ABSTRACT("Abstract", false),
        ACTIVE_CURATION("Active Curation", false),
        BOOK("Book", false),
        CHAPTER("Chapter", false),
        CURATION("Curation", false),
        JOURNAL("Journal", true),
        MOVIE("Movie", false),
        OTHER("Other", false),
        REVIEW("Review", false),
        UNKNOWN("Unknown", false),
        UNPUBLISHED("Unpublished", false),
        THESIS("Thesis", false);

        private final String display;

        Type(String type, Boolean allowCuration) {
            this.display = type;
        }

        public String getDisplay() {
            return display;
        }

        @Override
        public String toString() {
            return display;
        }

        public static Type fromString(String display) {
            for (Type type : values()) {
                if (type.display.equals(display)) {
                    return type;
                }
            }
            return null;
        }

    }

    public enum Status {
        ACTIVE("active"),
        INACTIVE("inactive"),
        EPUB("Epub ahead of print"),
        PRESS("in press");

        private final String display;

        Status(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        @Override
        public String toString() {
            return display;
        }

        public static Status fromString(String display) {
            for (Status status : values()) {
                if (status.display.equals(display)) {
                    return status;
                }
            }
            return null;
        }
    }

    public String getPrintable() {
        String printable = authors + " " + "(" + publicationDate.get(Calendar.YEAR) + ")" + " " + title + ". " + journal.getMedAbbrev() + " ";
        if (volume != null)
            printable =  printable + " " + volume + ":";
        if (pages != null)
            printable =  printable + " " + pages + ". ";
        if (status == Status.EPUB || status == Status.PRESS)
            printable = printable + status.toString() + ".";
        if (journal.isZfinDierectDataSubmission())
            printable += "(http://zfin.org).";
        return printable;
    }

}
