package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
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

@Setter
@Getter
public class Publication implements Comparable<Publication>, Serializable, EntityZdbID {

    @JsonView(View.Default.class)
    private String zdbID;
    @JsonView(View.Default.class)
    private String title;
    private String authors;
    private Set<PubmedPublicationAuthor> authorPubs;
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
    public Set<PubmedPublicationAuthor> getAuthorPubs() {
        return authorPubs;
    }

    public void setAuthorPubs(Set<PubmedPublicationAuthor> authorPubs) {
        this.authorPubs = authorPubs;
    }
    private Set<ZebrashareEditor> zebrashareEditors;

    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
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

    public PublicationType getType() {
        return type;
    }

    public void setType(PublicationType type) {
        this.type = type;
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

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
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
