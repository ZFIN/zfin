package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.curation.PublicationNote;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.zebrashare.ZebrashareEditor;

import java.io.Serializable;
import java.util.*;

/**
 * ToDo:
 */
public class Publication implements Comparable<Publication>, Serializable, EntityZdbID {

    @JsonView(View.Default.class)
    private String zdbID;
    @JsonView(View.Default.class)
    private String title;
    private String authors;



    private List<PubmedPublicationAuthor> authorPubs;
    @JsonView(View.Default.class)
    private String shortAuthorList;
    private String abstractText;
    private String volume;
    private String pages;
    private PublicationType type;
    private Integer accessionNumber;
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
    @JsonView(View.PubTrackerAPI.class)
    @JsonProperty("registeredAuthors")
    private Set<Person> people;
    private SortedSet<MeshHeading> meshHeadings;
    private Set<PublicationNote> notes;
    private Set<PublicationDbXref> dbXrefs;
    private SortedSet<PublicationFile> files;
    private Set<PublicationTrackingHistory> statusHistory;
    private Set<CorrespondenceSentMessage> sentMessages;
    private Set<CorrespondenceReceivedMessage> receivedMessages;
    private Date lastCorrespondenceDate;
    private Set<PublicationProcessingChecklistEntry> processingChecklistEntries;

    private boolean deletable;
    private boolean canShowImages;

    private boolean curatable;

    private boolean indexed;
    private GregorianCalendar indexedDate;
    private Person indexedBy;
    public List<PubmedPublicationAuthor> getAuthorPubs() {
        return authorPubs;
    }

    public void setAuthorPubs(List<PubmedPublicationAuthor> authorPubs) {
        this.authorPubs = authorPubs;
    }
    private Set<ZebrashareEditor> zebrashareEditors;

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

    public Person getIndexedBy() {
        return indexedBy;
    }

    public void setIndexedBy(Person indexedBy) {
        this.indexedBy = indexedBy;
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

    public PublicationType getType() {
        return type;
    }

    public void setType(PublicationType type) {
        this.type = type;
    }

    public Integer getAccessionNumber() {
        return accessionNumber;
    }

    @JsonView(View.PubTrackerAPI.class)
    @JsonProperty("pdfPath")
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

    public void setAccessionNumber(Integer accessionNumber) {
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

    public Set<PublicationTrackingHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(Set<PublicationTrackingHistory> statusHistory) {
        this.statusHistory = statusHistory;
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

    public Date getLastCorrespondenceDate() {
        return lastCorrespondenceDate;
    }

    public void setLastCorrespondenceDate(Date lastCorrespondenceDate) {
        this.lastCorrespondenceDate = lastCorrespondenceDate;
    }

    public Set<ZebrashareEditor> getZebrashareEditors() {
        return zebrashareEditors;
    }

    public void setZebrashareEditors(Set<ZebrashareEditor> zebrashareEditors) {
        this.zebrashareEditors = zebrashareEditors;
    }

    public Set<PublicationProcessingChecklistEntry> getProcessingChecklistEntries() {
        return processingChecklistEntries;
    }

    public void setProcessingChecklistEntries(Set<PublicationProcessingChecklistEntry> processingChecklistEntries) {
        this.processingChecklistEntries = processingChecklistEntries;
    }

    @JsonView({View.CitationsAPI.class, View.PubTrackerAPI.class})
    public String getCitation() {
        StringBuilder sb = new StringBuilder();
        sb.append(authors);
        if (publicationDate != null) {
            sb.append(" (");
            sb.append(getYear());
            sb.append(")");
        }
        sb.append(" ").append(title);
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
        if (volume != null) {
            sb.append(volume);
        }
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
        return !type.isPublished();
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

    public boolean getCuratable() {
        return curatable;
    }

    public void setCuratable(boolean curatable) {
        this.curatable = curatable;
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
        StringBuilder sb = new StringBuilder(getCitation());
        if (status == Status.EPUB || status == Status.PRESS) {
            sb.append(". ").append(status.toString());
        }
        if (journal.isZfinDierectDataSubmission()) {
            sb.append(". (http://zfin.org)");
        }
        sb.append(".");
        return sb.toString();
    }

    @JsonView(View.CitationsAPI.class)
    public String getIndexedOpenStatus() {
        if (!ProfileService.isRootUser())
            return "";
        String ret = isOpen() ? "OPEN" : "CLOSED";
        if (isIndexed())
            ret += ", INDEXED";
        return ret;
    }
}
