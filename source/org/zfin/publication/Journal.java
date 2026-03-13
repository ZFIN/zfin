package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;
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
@Setter
@Getter
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
    private boolean nice;

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

    public String getMedAbbrevOrDefault() {
        if (medAbbrev == null) {
            return abbreviation;
        }
        return medAbbrev;
    }

    public String getIsoAbbrevOrDefault() {
        if (isoAbbrev == null) {
            return abbreviation;
        }
        return isoAbbrev;
    }

    public boolean isZfinDirectDataSubmission() {
        if (medAbbrev != null && medAbbrev.equals("ZFIN Direct Data Submission"))
            return true;
        if (abbreviation != null && abbreviation.equals("ZFIN Direct Data Submission"))
            return true;

        return false;
    }
}
