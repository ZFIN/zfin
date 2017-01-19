package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Ontology-related namings.
 */
public enum OntologyDTO implements IsSerializable {

    ANATOMY(0, "AO", "zebrafish_anatomy", false, "ZFA:0000037") {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_QUALITIES;
        }
    },
    ANATOMY_FULL(10000, "AO + Stages", "zebrafish_anatomical_ontology", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_QUALITIES;
        }
    },
    DISEASE_ONTOLOGY(20, "Human Disease", "disease_ontology", false, "DOID:4") {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    // full GO ontology
    GO(1, "GO", "cellular_component,molecular_function,biological_process", true) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY;
        }
    },
    // full GO ontology
    GO_ONTOLOGY(1, "GO Ontology", "cellular_component,molecular_function,biological_process", true) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY;
        }
    },
    AOGO(1, "AOGO", "zebrafish_anatomy,cellular_component,molecular_function,biological_process", true) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    AOGODO(1, "AOGODO", "zebrafish_anatomy,cellular_component,molecular_function,biological_process,disease_ontology", true) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    // Subset of GO: Cellular Components
    GO_CC(2, "GO-CC", "cellular_component", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_QUALITIES;
        }
    },
    // Subset of GO: Molecular Function
    GO_MF(3, "GO-MF", "molecular_function", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_PROCESSES;
        }
    },
    // Subset of GO: Biological Process
    GO_BP(4, "GO-BP", "biological_process", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_PROCESSES;
        }
    },
    // PATO
    QUALITY(5, "Quality - ALL", "quality", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    QUALITY_PROCESSES(6, "Quality - Processes", "quality", "quality.process", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    QUALITY_QUALITIES(7, "Quality - Objects", "quality", "quality.quality", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    QUALITY_OBJECT_RELATIONAL(8, "Quality - Relational Objects", "quality", "quality.object.relational", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    QUALITY_PROCESSES_RELATIONAL(9, "Quality - Relational Processes", "quality", "quality.process.relational", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    QUALITY_QUALITATIVE(10, "Quality - Qualitative", "quality", "quality.qualitative", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    GO_BP_MF(12, "GO-BP / GO-MF", "biological_process,molecular_function", true) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_PROCESSES;
        }
    },
    SPATIAL(13, "Spatial", "spatial", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_QUALITIES;
        }
    },
    STAGE(14, "stages", "zebrafish_stages", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    MPATH(15, "MPATH", "mouse_pathology.ontology", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },


     MPATH_NEOPLASM(16, "MPATH-Neoplasm","mouse_pathology.ontology","mpath_neoplasm", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
     SO(17, "SO","sequence", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    ZECO(15, "ZECO", "zebrafish_experimental_conditions_ontology", false, "ZECO:0000100") {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    CHEBI(15, "CHEBI", "chebi_ontology", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return QUALITY_QUALITIES;
        }
    },
    ZECO_TAXONONY(19, "Zeco Taxonomy", "zeco_taxonomy", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    },
    BEHAVIOR(18, "BEHAVIOR", "behavior_ontology", false) {
        @Override
        public OntologyDTO getAssociatedQualityOntology() {
            return null;
        }
    };

    private int index;
    private String displayName;
    private String ontologyName;
    private boolean composedOntologies;
    private String subtreeOntology;
    private String rootTermID;

    private OntologyDTO(int index, String value, String name, boolean composedOntology) {
        this.index = index;
        this.displayName = value;
        this.ontologyName = name;
        this.composedOntologies = composedOntology;
    }

    private OntologyDTO(int index, String value, String name, boolean composedOntology, String rootTermID) {
        this.index = index;
        this.displayName = value;
        this.ontologyName = name;
        this.composedOntologies = composedOntology;
        this.rootTermID = rootTermID;
    }

    private OntologyDTO(int index, String value, String subtreeOntology, String name, boolean composedOntology) {
        this.index = index;
        this.displayName = value;
        this.ontologyName = name;
        this.composedOntologies = composedOntology;
        this.subtreeOntology = subtreeOntology;
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

    public static OntologyDTO getOntologyByName(String name) {
        for (OntologyDTO ontology : values()) {
            if (ontology.getOntologyName().equals(name))
                return ontology;
        }
        return null;
    }

    public List<OntologyDTO> getComposedOntologies(){
        if(!composedOntologies)
            return null;

        String[] individualOntologies = ontologyName.split(",");
        List<OntologyDTO> composedOntologies = new ArrayList<OntologyDTO>(individualOntologies.length);
        for(String individualOntology: individualOntologies)
            composedOntologies.add(getOntologyByName(individualOntology));
        return composedOntologies;
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

    public abstract OntologyDTO getAssociatedQualityOntology();

    public String getSubtreeOntology() {
        return subtreeOntology;
    }

    public boolean isSubtreeOntology(OntologyDTO ontology) {
        return (subtreeOntology != null && subtreeOntology.equals(ontology.getOntologyName()));
    }

    public boolean isComposed() {
        return composedOntologies;
    }

    public String getRootTermID() {
        return rootTermID;
    }
}

