package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class TermDTO extends RelatedEntityDTO implements IsSerializable {

    private String termOboID;
    private String definition;
    private String comment;
    private OntologyDTO ontology;

    public String getTermName() {
        return name;
    }

    public void setTermName(String termName) {
        this.name = termName;
    }

    public String getTermID() {
        return zdbID;
    }

    public void setTermID(String termID) {
        zdbID = termID;
    }

    public String getTermOboID() {
        return termOboID;
    }

    public void setTermOboID(String termOboID) {
        this.termOboID = termOboID;
    }

    public OntologyDTO getOntology() {
        return ontology;
    }

    public void setOntology(OntologyDTO ontology) {
        this.ontology = ontology;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermDTO termDTO = (TermDTO) o;

        if (zdbID != null ? !zdbID.equals(termDTO.zdbID) : termDTO.zdbID != null)
            return false;

        return true;
    }

    @Override
    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
    public int hashCode() {
        return 31 + (zdbID != null ? zdbID.hashCode() : 0);
    }

    public int compareTo(Object o) {
        if (o == null)
            return 1;
        if (!(o instanceof TermDTO))
            return 1;
        TermDTO term = (TermDTO) o;
        return term.compareTo(term.getName());

    }

    /**
     * Checks equality based on term name and Ontology only. This is needed for checking if a
     * new proposed post-composed term already exists.
     * The IDs are not known in our current auto-complete but only the names so we cannot check by ID.
     *
     * @param expressedTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnlyAndOntology(TermDTO expressedTerm) {
        if (!StringUtils.equals(name, expressedTerm.getTermName()))
            return false;
        if (ontology != expressedTerm.getOntology())
            return false;
        return true;
    }
}