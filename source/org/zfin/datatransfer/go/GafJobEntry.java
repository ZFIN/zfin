package org.zfin.datatransfer.go;

import org.zfin.mutant.MarkerGoTermEvidence;

/**
 * This is a placeholder for GafJobData.
 * It is used in place of MarkerGoTermEvidence to indicate what entries exist or should be removed.
 */
public class GafJobEntry {
    private String zdbID;
    private String entryString;

    public GafJobEntry(String zdbID) {
        this.zdbID = zdbID;
    }

    public GafJobEntry(MarkerGoTermEvidence markerGoTermEvidence) {
        zdbID = markerGoTermEvidence.getZdbID();
        entryString = markerGoTermEvidence.toString();
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getEntryString() {
        return entryString;
    }

    public void setEntryString(String entryString) {
        this.entryString = entryString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GafJobEntry that = (GafJobEntry) o;

        if (zdbID != null && that.zdbID != null) {
            return zdbID.equals(that.zdbID);
        }
        if (entryString != null ? !entryString.equals(that.entryString) : that.entryString != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (entryString != null ? entryString.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AnnotationRepresentation");
        sb.append("{value='").append(entryString).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
