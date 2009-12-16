package org.zfin.ontology;

/**
 * Dictionary of ontologies used in the TERM table.
 */
public enum Ontology {

    ANATOMY("anatomy"),
    // full GO ontology
    GO("gene ontology"),
    // Subset of GO: Cellular Components
    GO_CC("cellular_component"),
    // Subset of GO: Molecular Function
    GO_MF("molecular_function"),
    // Subset of GO: Biological Process
    GO_BP("biological_process"),
    // PATO
    QUALITY("pato.quality");

    private String ontologyName;

    private Ontology(String name) {
        this.ontologyName = name;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public static Ontology getOntology(String name) {
        for (Ontology ontology : values()) {
            if (ontology.getOntologyName().equals(name))
                return ontology;
        }
        return null;
    }

}
