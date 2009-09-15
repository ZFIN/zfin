package org.zfin.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class ExpressedTermDTO implements IsSerializable, Comparable<ExpressedTermDTO> {

    private String subtermID;
    private String subtermName;
    private String supertermID;
    private String supertermName;
    private String supertermOboID;
    private String subtermOboID;
    private String subtermOntology;

    private boolean expressionFound;

    public String getSubtermName() {
        return subtermName;
    }

    public void setSubtermName(String subtermName) {
        this.subtermName = subtermName;
    }

    public String getSupertermName() {
        return supertermName;
    }

    public void setSupertermName(String supertermName) {
        this.supertermName = supertermName;
    }

    public String getSubtermID() {
        return subtermID;
    }

    public void setSubtermID(String subtermID) {
        this.subtermID = subtermID;
    }

    public String getSupertermID() {
        return supertermID;
    }

    public void setSupertermID(String supertermID) {
        this.supertermID = supertermID;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public String getComposedTerm() {
        String composedTerm = supertermName;
        if (subtermName != null)
            composedTerm += ":" + subtermName;
        return composedTerm;
    }

    public String getSupertermOboID() {
        return supertermOboID;
    }

    public void setSupertermOboID(String supertermOboID) {
        this.supertermOboID = supertermOboID;
    }

    public String getSubtermOboID() {
        return subtermOboID;
    }

    public void setSubtermOboID(String subtermOboID) {
        this.subtermOboID = subtermOboID;
    }

    public String getSubtermOntology() {
        return subtermOntology;
    }

    public void setSubtermOntology(String subtermOntology) {
        this.subtermOntology = subtermOntology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressedTermDTO that = (ExpressedTermDTO) o;

        if (subtermID != null ? !subtermID.equals(that.subtermID) : that.subtermID != null) return false;
        if (supertermID != null ? !supertermID.equals(that.supertermID) : that.supertermID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subtermID != null ? subtermID.hashCode() : 0;
        result = 31 * result + (supertermID != null ? supertermID.hashCode() : 0);
        return result;
    }

    public String getUniqueID() {
        String composedID = supertermID;
        if (subtermID != null)
            composedID += ":" + subtermID;
        return composedID;
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        // if the superterms are different sort by superterm
        if (!supertermName.equals(o.getSupertermName()))
            return supertermName.compareTo(o.getSupertermName());

        // if superterms are the same sort by suybterm.
        if (subtermName == null && o.getSubtermName() != null)
            return -1;
        if (subtermName != null && o.getSubtermName() == null)
            return 1;
        if (subtermName == null && o.getSubtermName() == null)
            return supertermName.compareTo(o.getSupertermName());

        return subtermName.compareTo(o.getSubtermName());
    }

    public static enum Ontology {
        AO, GO
    }
}