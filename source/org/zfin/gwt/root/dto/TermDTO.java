package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class TermDTO implements IsSerializable, Comparable<TermDTO> {

    private String termID;
    protected String termName;
    private String termOboID;
    private String definition;
    private String comment;
    private OntologyDTO ontology;

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getTermID() {
        return termID;
    }

    public void setTermID(String termID) {
        this.termID = termID;
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

        if (termID != null ? !termID.equals(termDTO.termID) : termDTO.termID != null)
            return false;

        return true;
    }

    @Override
    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
    public int hashCode() {
        return 31 + (termID != null ? termID.hashCode() : 0);
    }

    public int compareTo(TermDTO o) {
        if (o == null)
            return 1;
        return termName.compareTo(o.getTermName());

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
        if (!StringUtils.equals(termName, expressedTerm.getTermName()))
            return false;
        if (ontology != expressedTerm.getOntology())
            return false;
        return true;
    }
}