package org.zfin.framework.presentation.client;

/**
 * Ontology-related namings.
 */
public enum Ontology {
    
    ANATOMY(0, "AO", "Anatomy"),
    // full GO ontology
    GO(1, "GO", "Gene Ontology"),
    // Subset of GO: Cellular Components
    GO_CC(2, "GO-CC", "Cellular Component"),
    // Subset of GO: Molecular Function
    GO_MF(3, "GO-MF", "Molecular Function"),
    // Subset of GO: Biological Process
    GO_BP(4, "GO-BP", "Biological Process"),
    // PATO
    QUALITY(5, "Quality", "Quality");

    private int index;
    private String displayName;
    private String ontologyName;

    private Ontology(int index, String value, String name) {
        this.index = index;
        this.displayName = value;
        this.ontologyName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public static Ontology getOntologyByDisplayName(String name) {
        for (Ontology ontology : values()) {
            if (ontology.getDisplayName().equals(name))
                return ontology;
        }
        return null;
    }

    public static Ontology getOntologyByDescriptor(String ontologyName) {
        for (Ontology ontology : values()) {
            if (ontology.getOntologyName().equals(ontologyName))
                return ontology;
        }
        return null;
    }

    public String getDBName(){
        return ontologyName.toLowerCase().replaceAll(" ", "_");
    }
}

