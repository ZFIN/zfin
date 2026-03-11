package org.zfin.publication;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.SourceAlias;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;

/**
 * Journal domain object.
 */
@Entity
@Table(name = "journal")
public class Journal implements Serializable, EntityZdbID {

    @Id
    @Column(name = "jrnl_zdb_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zdbIdGeneratorForJournal")
    @GenericGenerator(name = "zdbIdGeneratorForJournal",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "JRNL"),
                    @Parameter(name = "insertActiveSource", value = "true")
            })
    private String zdbID;

    @Column(name = "jrnl_name", nullable = false)
    private String name;

    @Column(name = "jrnl_abbrev", nullable = false)
    private String abbreviation;

    @Column(name = "jrnl_medabbrev")
    private String medAbbrev;

    @Column(name = "jrnl_isoabbrev")
    private String isoAbbrev;

    @Column(name = "jrnl_publisher")
    private String publisher;

    @Column(name = "jrnl_print_issn")
    private String printIssn;

    @Column(name = "jrnl_online_issn")
    private String onlineIssn;

    @Column(name = "jrnl_nlmid")
    private String nlmID;

    @Transient
    private SortedSet<Publication> publications;

    @OneToMany
    @JoinColumn(name = "salias_source_zdb_id")
    @OrderBy("salias_alias_lower DESC")
    private Set<SourceAlias> aliases;

    @Column(name = "jrnl_is_nice")
    private boolean isNice;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    @Override
    public String getEntityType() {
        return "Journal";
    }

    @Override
    public String getEntityName() {
        return name;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPrintIssn() { return printIssn; }

    public void setPrintIssn(String printIssn) { this.printIssn = printIssn; }

    public String getOnlineIssn() { return onlineIssn; }

    public void setOnlineIssn(String onlineIssn) { this.onlineIssn = onlineIssn; }


    public String getNlmID() { return nlmID; }

    public void setNlmID(String nlmID) { this.nlmID = nlmID; }

    public boolean isNice() { return isNice; }

    public boolean getIsNice() {
        return isNice;
    }

    public void setIsNice(boolean isNice) {
        this.isNice = isNice;
    }

    public SortedSet<Publication> getPublications() {
        return publications;
    }

    public void setPublications(SortedSet<Publication> publications) {
        this.publications = publications;
    }

    public Set<SourceAlias> getAliases() {
        return aliases;
    }

    public void setAliases(Set<SourceAlias> aliases) {
        this.aliases = aliases;
    }

    public String getRawMedAbbrev() {
        return medAbbrev;
    }

    public String getMedAbbrev() {
        if (medAbbrev == null)
            return abbreviation;

        return medAbbrev;
    }

    public String getRawIsoAbbrev() {
        return isoAbbrev;
    }

    public String getIsoAbbrev() {
        if (isoAbbrev == null)
            return abbreviation;
        return isoAbbrev;
    }

    public void setIsoAbbrev(String isoAbbrev) {
        this.isoAbbrev = isoAbbrev;
    }

    public void setMedAbbrev(String medAbbrev) {
        this.medAbbrev = medAbbrev;
    }

    public boolean isZfinDierectDataSubmission() {
        if (medAbbrev != null && medAbbrev.equals("ZFIN Direct Data Submission"))
            return true;
        if (abbreviation != null && abbreviation.equals("ZFIN Direct Data Submission"))
            return true;

        return false;
    }
}
