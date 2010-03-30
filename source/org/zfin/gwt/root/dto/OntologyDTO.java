package org.zfin.gwt.root.dto;

/**
 * Ontology-related namings.
 */
public enum OntologyDTO {

    ANATOMY(0, "AO", "zebrafish_anatomy"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY_QUALITIES;
        }
    },
    // full GO ontology
    GO(1, "GO", "cellular_component,molecular_function,biological_process"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY;
        }
    },
    // Subset of GO: Cellular Components
    GO_CC(2, "GO-CC", "cellular_component"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY_QUALITIES;
        }
    },
    // Subset of GO: Molecular Function
    GO_MF(3, "GO-MF", "molecular_function"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY_PROCESSES;
        }
    },
    // Subset of GO: Biological Process
    GO_BP(4, "GO-BP", "biological_process"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY_PROCESSES;
        }
    },
    // PATO
    QUALITY(5, "Quality - ALL", "pato.quality"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    },
    QUALITY_PROCESSES(6, "Quality - Processes", "pato.quality.process"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    },
    QUALITY_QUALITIES(7, "Quality - Objects", "pato.quality.quality"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    },
    QUALITY_QUALITIES_RELATIONAL(8, "Quality - Relational Objects", "pato.quality.object.relational"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    },
    QUALITY_PROCESSES_RELATIONAL(9, "Quality - Relational Processes", "pato.quality.process.relational"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    },
    QUALITY_QUALITATIVE(10, "Quality - Qualitative", "pato.quality.qualitative"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    },
    GO_MF_CC(11, "GO-MF / GO-CC", "molecular_function,cellular_component"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY;
        }
    },
    GO_BP_MF(12, "GO-BP / GO-MF", "biological_process,molecular_function"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return QUALITY_PROCESSES;
        }
    },
    SPATIAL(13, "spatial", "spatial"){
        @Override
        public OntologyDTO getAssociatedQualityOntology(){
            return null;
        }
    };

    private int index;
    private String displayName;
    private String ontologyName;

    private OntologyDTO(int index, String value, String name) {
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

    public static OntologyDTO getOntologyByDisplayName(String name) {
        for (OntologyDTO ontology : values()) {
            if (ontology.getDisplayName().equals(name))
                return ontology;
        }
        return null;
    }

    public static OntologyDTO getOntologyByDescriptor(String descriptorName) {
        for (OntologyDTO ontology : values()) {
            if (ontology.getOntologyName().equals(descriptorName))
                return ontology;
        }
        return null;
    }

    public String getDBName() {
        return ontologyName.toLowerCase().replaceAll(" ", "_");
    }

    public static boolean isGoOntology(OntologyDTO name) {
        if (name == null)
            return false;
        if (name.equals(GO_BP))
            return true;
        if (name.equals(GO_CC))
            return true;
        if (name.equals(GO_MF))
            return true;
        return false;
    }

    /**
     * Convenience method to convert the GO_TERM table ontology name into TERM table ontology
     *
     * @param subOntologyName sub ontology name
     * @return Ontology 
     */
    public static OntologyDTO getOntologyByGoDescriptor(String subOntologyName) {
        if (subOntologyName == null)
            return null;

        if (subOntologyName.equals("Molecular Function"))
            return GO_MF;
        if (subOntologyName.equals("Cellular Component"))
            return GO_CC;
        if (subOntologyName.equals("Biological Process"))
            return GO_BP;
        return null;
    }

    public abstract OntologyDTO getAssociatedQualityOntology();

}

