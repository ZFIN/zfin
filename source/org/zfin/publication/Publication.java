package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.curation.PublicationNote;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.zebrashare.ZebrashareEditor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;

@Setter
@Getter
@Entity
@Table(name = "Publication")
public class Publication implements Comparable<Publication>, Serializable, EntityZdbID {

    @JsonView({View.Default.class, View.API.class, View.ExpressionPublicationUI.class})
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Publication")
    @GenericGenerator(name = "Publication",
        strategy = "org.zfin.database.ZdbIdGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "type", value = "PUB"),
            @org.hibernate.annotations.Parameter(name = "insertActiveSource", value = "true")
        })
    @Column(name = "zdb_id")
    private String zdbID;
    @JsonView(View.Default.class)
    @Column(name = "title")
    private String title;
    @Column(name = "authors")
    private String authors;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ppa_publication_zdb_id")
    private Set<PubmedPublicationAuthor> authorPubs;
    @JsonView({View.Default.class, View.API.class})
    @Column(name = "pub_mini_ref")
    private String shortAuthorList;
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "pub_abstract")
    private String abstractText;
    @Column(name = "pub_volume")
    private String volume;
    @Column(name = "pub_pages")
    private String pages;
    @Column(name = "jtype")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.publication.PublicationType")})
    private PublicationType type;
    @Column(name = "accession_no")
    @Basic(fetch = FetchType.LAZY)
    private Integer accessionNumber;
    @Column(name = "pub_doi")
    private String doi;
    @Column(name = "pub_acknowledgment")
    private String acknowledgment;
    @Column(name = "status")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.publication.Publication$Status")})
    private Status status;
    @Column(name = "keywords")
    private String keywords;
    @Column(name = "pub_errata_and_notes")
    private String errataAndNotes;
    @Column(name = "pub_date")
    private GregorianCalendar publicationDate;
    @Column(name = "pub_completion_date")
    private GregorianCalendar closeDate;
    @Column(name = "pub_arrival_date")
    private GregorianCalendar entryDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_jrnl_zdb_id")
    private Journal journal;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatex_source_zdb_id")
    private Set<ExpressionExperiment2> expressionExperiments;
    @JsonView(View.FigureAPI.class)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "fig_source_zdb_id")
    private Set<Figure> figures;
    //yes, should be authors, but that conflicts with the string field
    @JsonView(View.PubTrackerAPI.class)
    @JsonProperty("registeredAuthors")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "int_person_pub", joinColumns = {
        @JoinColumn(name = "target_id", nullable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "source_id",
            nullable = false, updatable = false)})
    @OrderBy(value = "full_name asc")
    private Set<Person> people;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_pub_zdb_id")
    @OrderBy()
    private SortedSet<MeshHeading> meshHeadings;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pnote_pub_zdb_id")
    @OrderBy("date desc")
    private Set<PublicationNote> notes;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdx_pub_zdb_id")
    private Set<PublicationDbXref> dbXrefs;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pf_pub_zdb_id")
    @OrderBy("originalFileName")
    private SortedSet<PublicationFile> files;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pth_pub_zdb_id")
    private Set<PublicationTrackingHistory> statusHistory;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pubcst_pub_zdb_id")
    private Set<CorrespondenceSentMessage> sentMessages;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pubcre_pub_zdb_id")
    private Set<CorrespondenceReceivedMessage> receivedMessages;
    @Column(name = "pub_last_correspondence_date")
    private Date lastCorrespondenceDate;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ppc_pub_zdb_id")
    private Set<PublicationProcessingChecklistEntry> processingChecklistEntries;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "zdep_pub_zdb_id")
    private Set<ZebrashareEditor> zebrashareEditors;

    @Transient
    private boolean deletable;
    @Column(name = "pub_can_show_images")
    private boolean canShowImages;

    @Column(name = "pub_is_curatable")
    private boolean curatable;

    @Column(name = "pub_is_indexed")
    private boolean indexed;
    @Column(name = "pub_indexed_date")
    private GregorianCalendar indexedDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_indexed_by")
    private Person indexedBy;

    public Set<PubmedPublicationAuthor> getAuthorPubs() {
        return authorPubs;
    }

    public void setAuthorPubs(Set<PubmedPublicationAuthor> authorPubs) {
        this.authorPubs = authorPubs;
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
        if (!(otherPublication instanceof Publication publication)) {
            return false;
        }

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
