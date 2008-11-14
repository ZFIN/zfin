package org.zfin;

import org.zfin.infrastructure.PublicationAttribution;

import java.util.Set;

/**
 * Domain object for ZFIN.
 */
public abstract class ExternalNote {

    private String zdbID;
    private String note;
    private String type;
    protected Set<PublicationAttribution> pubAttributions;

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

}
