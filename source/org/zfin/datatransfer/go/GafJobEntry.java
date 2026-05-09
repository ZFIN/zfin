package org.zfin.datatransfer.go;

import org.zfin.mutant.MarkerGoTermEvidence;

/**
 * This is a placeholder for GafJobData.
 * It is used in place of MarkerGoTermEvidence to indicate what entries exist or should be removed.
 */
public class GafJobEntry {
    private String zdbID;
    private String entryString;

    // Captured at construction time so downstream consumers (e.g. report builders)
    // can render structured columns without re-parsing the toString() blob.
    private String marker;
    private String evidenceCode;
    private String qualifierRelation;
    private String source;
    private String goTermName;
    private String goTermID;
    private String organizationCreatedBy;

    public GafJobEntry(String zdbID) {
        this.zdbID = zdbID;
    }

    public GafJobEntry(MarkerGoTermEvidence m) {
        this.zdbID       = m.getZdbID();
        this.entryString = m.toString();
        this.marker                = m.getMarker()            != null ? m.getMarker().getAbbreviation()       : null;
        this.evidenceCode          = m.getEvidenceCode()      != null ? m.getEvidenceCode().getName()         : null;
        this.qualifierRelation     = m.getQualifierRelation() != null ? m.getQualifierRelation().getTermName(): null;
        this.source                = m.getSource()            != null ? m.getSource().getZdbID()              : null;
        this.goTermName            = m.getGoTerm()            != null ? m.getGoTerm().getTermName()           : null;
        this.goTermID              = m.getGoTerm()            != null ? m.getGoTerm().getOboID()              : null;
        this.organizationCreatedBy = m.getOrganizationCreatedBy();
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

    public String getMarker()                { return marker; }
    public String getEvidenceCode()          { return evidenceCode; }
    public String getQualifierRelation()     { return qualifierRelation; }
    public String getSource()                { return source; }
    public String getGoTermName()            { return goTermName; }
    public String getGoTermID()              { return goTermID; }
    public String getOrganizationCreatedBy() { return organizationCreatedBy; }

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
