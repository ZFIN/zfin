package org.zfin.ontology;

import java.io.Serializable;
import java.util.*;

/**
 * Dictionary of ontologies used in the TERM table.
 */
public enum Ontology implements Serializable {

    ANATOMY("zebrafish_anatomy", "Anatomy Ontology", false),
    // full GO ontology
    GO("cellular_component,molecular_function,biological_process", "Gene Ontology", false),
    // Subset of GO: Cellular Components
    GO_CC("cellular_component",  "Gene Ontology: Cellular Components",false),
    // Subset of GO: Molecular Function
    GO_MF("molecular_function",  "Gene Ontology: Molecular Functions",false),
    // Subset of GO: Biological Process
    GO_BP("biological_process",  "Gene Ontology: Biological Processes",false),
    // PATO
    QUALITY("pato.quality",  "Phenotype and Trait Ontology",false),
    // PATO: Processes
    QUALITY_PROCESSES("pato.quality.process",  "Phenotype and Trait Ontology: Quality of Processes",false),
    // PATO: Processes
    QUALITY_PROCESSES_RELATIONAL("pato.eq.quality.process.relational",  "Phenotype and Trait Ontology: Relation of Processes",false),
    // PATO: Qualities
    QUALITY_QUALITIES("pato.quality.quality",  "Phenotype and Trait Ontology: Quality of Qualities",false),
    // PATO: Qualitative
    QUALITY_QUALITATIVE("pato.eq.quality.qualitative",  "Phenotype and Trait Ontology: Quality of Qualities",false),
    // BP and MF
    // PATO: Processes
    QUALITY_OBJECT_RELATIONAL("pato.eq.quality.object.relational",  "Phenotype and Trait Ontology: Relation of Object",false),
    GO_BP_MF(GO_BP.getOntologyName() + "," + GO_MF.getOntologyName(),  "Gene Ontology: Biological Processes and Molecular Functions",true),
    // Spatial
    SPATIAL("spatial",  "Spatial Ontology",false);

    private String ontologyName;
    private String commonName;
    private boolean composedOntologies;

    private Ontology(String name, String commonName, boolean composed) {
        this.ontologyName = name;
        this.commonName = commonName;
        this.composedOntologies = composed;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public boolean isComposedOntologies() {
        return composedOntologies;
    }

    public String toString(){
        return ontologyName;
    }

    public String getCommonName(){
        return commonName;
    }

    public static Ontology getOntology(String name) {
        for (Ontology ontology : values()) {
            if (ontology.getOntologyName().equals(name))
                return ontology;
        }
        return null;
    }

    public static List<Ontology> getOntologies(String name) {
        if (name == null)
            return null;
        String[] ontologyNames = name.split(",");
        List<Ontology> ontologies = new ArrayList<Ontology>(5);
        for (String ontName : ontologyNames) {
            Ontology ontology = getOntology(ontName);
            if (ontology != null)
                ontologies.add(ontology);
        }
        return ontologies;
    }

    public static boolean isGoOntology(Ontology ontology) {
        if (ontology == null)
            return false;
        if (ontology.equals(GO_BP))
            return true;
        if (ontology.equals(GO_CC))
            return true;
        if (ontology.equals(GO_MF))
            return true;
        return false;
    }

    /**
     * Retrieve a set of ontologies this ontology is composed of.
     * @return set of ontologies
     */
    public Collection<Ontology> getIndividualOntologies() {
        if (!composedOntologies) {
            Collection<Ontology> ontologies = new HashSet<Ontology>(1);
            ontologies.add(this);
            return ontologies;
        }
        String[] ontNames = ontologyName.split(",");
        Collection<Ontology> ontologies = new HashSet<Ontology>(ontNames.length);
        for(String ontName: ontNames){
            ontologies.add(getOntology(ontName));
        }
        return ontologies;
    }

    /**
     * Retrieve a collection of ontologies that are sub-ontologies to a given root.
     * @param rootOntology root ontology
     * @return collection of ontologies.
     */
    public static Collection<Ontology> getSubOntologies(Ontology rootOntology) {
        Set<Ontology> subOntologies = new HashSet<Ontology>(3);
        String rootOntologyName = rootOntology.ontologyName;
        for(Ontology ontology : values()){
            if(ontology != rootOntology && ontology.getOntologyName().startsWith(rootOntologyName))
                subOntologies.add(ontology);
        }
        return subOntologies;
    }
}
