package org.zfin.gwt.root.dto;

/**
 */
public class GoTermDTO extends RelatedEntityDTO {

    private String subOntology;
    public static final String MOLECULAR_FUNCTION = "Molecular Function";
    public static final String BIOLOGICAL_PROCESS = "Biological Process";
    public static final String CELLULAR_COMPONENT = "Cellular Component";

    public String getSubOntology() {
        return subOntology;
    }

    public void setSubOntology(String subOntology) {
        this.subOntology = subOntology;
    }
}
