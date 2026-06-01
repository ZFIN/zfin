package org.zfin.datatransfer.go;

import org.apache.commons.collections4.CollectionUtils;
import org.zfin.mutant.MarkerGoTermAnnotationExtn;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    private String withFrom;
    private String annotationExtensions;
    private String noctuaModelId;

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
        this.withFrom              = formatWithFrom(m);
        this.annotationExtensions  = formatAnnotationExtensions(m);
        this.noctuaModelId         = m.getNoctuaModelId();
    }

    /**
     * GAF column 8 contents as a single string: a sorted, pipe-joined list of
     * the entry's inferredFrom identifiers (e.g. {@code "Rfam:RF00256|UniProtKB:P12345"}).
     * Empty string if the evidence carries no inferences.
     */
    public static String formatWithFrom(MarkerGoTermEvidence m) {
        Set<String> inferences = m.getInferencesAsString();
        if (CollectionUtils.isEmpty(inferences)) return "";
        return String.join("|", new TreeSet<>(inferences));
    }

    /**
     * GAF column 16 contents as a single string: pipe-joined {@code relation(identifier)}
     * pairs, e.g. {@code "part_of(GO:0005634)|occurs_in(CL:0000540)"}. The relation
     * term ZDB ID is resolved to its term name via the ontology repository when
     * possible; if the lookup is unavailable (e.g. the report runs outside a
     * Hibernate session) the raw ZDB ID is kept as a fallback so the cell is
     * never silently empty.
     */
    public static String formatAnnotationExtensions(MarkerGoTermEvidence m) {
        Set<MarkerGoTermAnnotationExtn> extns = m.getAnnotationExtensions();
        if (CollectionUtils.isEmpty(extns)) return "";
        List<String> parts = new ArrayList<>();
        for (MarkerGoTermAnnotationExtn e : extns) {
            String relation = e.getRelationshipTerm();
            try {
                GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(relation);
                if (term != null && term.getTermName() != null) relation = term.getTermName();
            } catch (RuntimeException ignored) {
                // Keep the raw ZDB ID — better than dropping the row.
            }
            String identifier = e.getIdentifierTermText() != null ? e.getIdentifierTermText() : "";
            parts.add(relation + "(" + identifier + ")");
        }
        java.util.Collections.sort(parts);
        return String.join("|", parts);
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
    public String getWithFrom()              { return withFrom; }
    public String getAnnotationExtensions()  { return annotationExtensions; }
    public String getNoctuaModelId()         { return noctuaModelId; }

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
