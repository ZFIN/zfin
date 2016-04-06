package org.zfin;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.infrastructure.PersonAttribution;
import org.zfin.infrastructure.PublicationAttribution;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain object for ZFIN.
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "extnote_note_type",
        discriminatorType = DiscriminatorType.STRING
)
@Table(name = "external_note")
public abstract class ExternalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "EXTNOTE"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "extnote_zdb_id")
    private String zdbID;
    @Column(name = "extnote_note")
    protected String note;
    @Column(name = "extnote_note_type", insertable = false, updatable = false)
    private String type;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recattrib_data_zdb_id")
    protected Set<PublicationAttribution> pubAttributions;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recattrib_data_zdb_id")
    protected Set<PersonAttribution> personAttributions;
    @Column(name = "extnote_data_zdb_id", insertable = false, updatable = false)
    private String externalDataZdbID;

    public String getExternalDataZdbID() {
        return externalDataZdbID;
    }

    public void setExternalDataZdbID(String externalDataZdbID) {
        this.externalDataZdbID = externalDataZdbID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<PublicationAttribution> getPubAttributions() {
        return pubAttributions;
    }

    public void setPubAttributions(Set<PublicationAttribution> pubAttributions) {
        this.pubAttributions = pubAttributions;
    }

    public Set<PersonAttribution> getPersonAttributions() {
        return personAttributions;
    }

    public void setPersonAttributions(Set<PersonAttribution> personAttributions) {
        this.personAttributions = personAttributions;
    }

    public void addPublicationAttribution(PublicationAttribution attribution) {
        if (pubAttributions == null)
            pubAttributions = new HashSet<>();
        pubAttributions.add(attribution);
    }


    public enum Type {
        ORTHOLOGY("orthology"),
        FEATURE("feature"),
        GENOTYPE("genotype"),
        CURATOR_NOTE("curator note"),
        ORIGINAL_SUBMITTER_COMMENTS("original submitter comments"),
        ANTIBODY("antibody");

        private String value;

        private Type(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }

    }
}
