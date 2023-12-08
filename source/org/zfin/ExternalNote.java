package org.zfin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.PersonAttribution;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.Set;

/**
 * Domain object for ZFIN.
 */

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "extnote_note_type",
        discriminatorType = DiscriminatorType.STRING
)
@Table(name = "external_note")
public class ExternalNote implements Comparable<ExternalNote> {

    @JsonView(View.API.class)
    @Column(name = "extnote_note")
    protected String note;
    @Column(name = "extnote_tag")
    protected String tag;
    @ManyToOne
    @JoinColumn(name = "extnote_source_zdb_id")
    protected Publication publication;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recattrib_data_zdb_id")

    protected Set<PersonAttribution> personAttributions;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExternalNote")
    @GenericGenerator(name = "ExternalNote",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "EXTNOTE"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @JsonView(View.API.class)
    @Column(name = "extnote_zdb_id")
    private String zdbID;
    @Column(name = "extnote_note_type", insertable = false, updatable = false)
    private String type;
    @JsonView(View.API.class)
    @JsonProperty("dataZdbID")
    @Column(name = "extnote_data_zdb_id", insertable = false, updatable = false)
    private String externalDataZdbID;

    public int compareTo(ExternalNote note) {
        if (note.publication == null)
            return -1;
        if (publication == null)
            return +1;

        int publicationComparison = publication.compareTo(note.publication);
        if (publicationComparison != 0)
            return publicationComparison;

        // handle case the notes have the same publication
        // compare according to date it got created
        return getZdbID().compareTo(note.getZdbID());
    }


    public enum Type {
        ORTHOLOGY("orthology"),
        FEATURE("feature"),
        VARIANT("variant"),
        FEATUREVARIANT("featurevariant"),
        GENOTYPE("genotype"),
        CURATOR_NOTE("curator note"),
        ORIGINAL_SUBMITTER_COMMENTS("original submitter comments"),
        ANTIBODY("antibody");

        private String value;

        private Type(String value) {
            this.value = value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }

        public String toString() {
            return this.value;
        }

    }
}
