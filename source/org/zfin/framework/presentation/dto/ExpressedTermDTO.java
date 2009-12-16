package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.presentation.client.Ontology;
import org.zfin.framework.presentation.gwtutils.StringUtils;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class ExpressedTermDTO implements IsSerializable, Comparable<ExpressedTermDTO> {

    private String supertermID;
    private String supertermName;
    private String supertermOboID;
    private String subtermID;
    private String subtermName;
    private String subtermOboID;
    private String qualityID;
    private String qualityName;
    private String qualityOboID;
    private Ontology subtermOntology;
    private Ontology supertermOntology;
    private Ontology qualityOntology;


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

    public String getDisplayName() {
        String composedTerm = supertermName;
        if (subtermName != null)
            composedTerm += ":" + subtermName;
        if (qualityName != null)
            composedTerm += ":" + qualityName;
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

    public Ontology getSubtermOntology() {
        return subtermOntology;
    }

    public void setSubtermOntology(Ontology subtermOntology) {
        this.subtermOntology = subtermOntology;
    }

    public String getQualityID() {
        return qualityID;
    }

    public void setQualityID(String qualityID) {
        this.qualityID = qualityID;
    }

    public String getQualityName() {
        return qualityName;
    }

    public void setQualityName(String qualityName) {
        this.qualityName = qualityName;
    }

    public String getQualityOboID() {
        return qualityOboID;
    }

    public void setQualityOboID(String qualityOboID) {
        this.qualityOboID = qualityOboID;
    }

    public Ontology getSupertermOntology() {
        return supertermOntology;
    }

    public void setSupertermOntology(Ontology supertermOntology) {
        this.supertermOntology = supertermOntology;
    }

    public Ontology getQualityOntology() {
        return qualityOntology;
    }

    public void setQualityOntology(Ontology qualityOntology) {
        this.qualityOntology = qualityOntology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressedTermDTO that = (ExpressedTermDTO) o;

        if (subtermID != null ? !subtermID.equals(that.subtermID) : that.subtermID != null)
            return false;
        if (supertermID != null ? !supertermID.equals(that.supertermID) : that.supertermID != null)
            return false;

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

    /**
     * Checks equality based on term names only. This is needed for checking if a
     * new proposed post-composed term already exists.
     *
     * @param expressedTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnly(ExpressedTermDTO expressedTerm) {
        if (!StringUtils.equals(supertermName, expressedTerm.getSupertermName()))
            return false;
        if (!StringUtils.equals(subtermName, expressedTerm.getSubtermName()))
            return false;
        if (!StringUtils.equals(qualityName, expressedTerm.getQualityName()))
            return false;
        return true;
    }
}