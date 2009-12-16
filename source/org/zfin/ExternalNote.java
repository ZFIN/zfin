package org.zfin;

import org.zfin.infrastructure.PersonAttribution;
import org.zfin.infrastructure.PublicationAttribution;

import java.util.Set;

/**
 * Domain object for ZFIN.
 */
public abstract class ExternalNote {

    private String zdbID;
    protected String note;
    private String type;
    protected Set<PublicationAttribution> pubAttributions;
    protected Set<PersonAttribution> personAttributions;

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

    public enum Type {
        ORTHOLOGY("orthology"),
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
