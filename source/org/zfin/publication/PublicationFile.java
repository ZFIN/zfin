package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.framework.api.View;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinPropertiesEnum;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "publication_file")
public class PublicationFile implements Comparable<PublicationFile> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pf_pk_id")
    @JsonView(View.Default.class)
    private long id;

    @ManyToOne
    @JoinColumn(name = "pf_pub_zdb_id")
    private Publication publication;

    @Column(name = "pf_file_name")
    private String fileName;

    @Column(name = "pf_original_file_name")
    @JsonView(View.Default.class)
    private String originalFileName;

    @ManyToOne
    @JoinColumn(name = "pf_file_type_id")
    @JsonView(View.Default.class)
    private PublicationFileType type;

    /** When the row was added to the DB (set by the column default, never by Hibernate). */
    @Column(name = "pf_date_entered", insertable = false, updatable = false)
    @JsonView(View.Default.class)
    private Date dateEntered;

    /** Last-modified time of the file on disk; not persisted, null if the file is missing. */
    @Transient
    @JsonView(View.Default.class)
    private Date modifiedOnDisk;

    @Column(name = "pf_source")
    @Enumerated(EnumType.STRING)
    @JsonView(View.Default.class)
    private PublicationFileSource source;

    /** Who added the file: the uploading user for manual uploads; null for automated loads and unknown. */
    @ManyToOne
    @JoinColumn(name = "pf_person_zdb_id")
    private Person person;

    @Transient
    @JsonView(View.Default.class)
    private long size;

    /** For display: the uploading person's name, else the source label, else null. Not persisted. */
    @Transient
    @JsonView(View.Default.class)
    private String addedBy;

    @JsonView(View.Default.class)
    public String getFullPath() {
        return ZfinPropertiesEnum.PDF_LOAD + "/" + fileName;
    }

    @Override
    public int compareTo(PublicationFile o) {
        int typeCompare = type.compareTo(o.getType());
        if (typeCompare != 0) {
            return typeCompare;
        }
        int nameCompare = ObjectUtils.compare(originalFileName, o.getOriginalFileName());
        if (nameCompare != 0) {
            return nameCompare;
        }
        // Final tiebreaker on the PK so distinct rows that share a type and filename
        // (e.g. duplicate Original Article uploads) are never collapsed when held in a SortedSet.
        return Long.compare(id, o.getId());
    }
}
