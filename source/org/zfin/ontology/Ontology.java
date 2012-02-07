package org.zfin.ontology;

import java.io.Serializable;
import java.util.*;

/**
 * Dictionary of ontologies used in the TERM table.
 */
public enum Ontology implements Serializable {

    ANATOMY("zebrafish_anatomy", "Anatomy Ontology", false),
    ANATOMY_FULL("zebrafish_anatomical_ontology", "Anatomy Ontology including stage ontology", false),
    // stages
    STAGE("zebrafish_stages", "Zebrafish Stage Ontology", false),
    GO_ONTOLOGY("gene_ontology", "Full Gene Ontology: Default namespace", true),
    // full GO ontology
    GO("cellular_component,molecular_function,biological_process", "Gene Ontology", true),
    // Subset of GO: Cellular Components
    GO_CC("cellular_component", "Gene Ontology: Cellular Component", false),
    // Subset of GO: Molecular Function
    GO_MF("molecular_function", "Gene Ontology: Molecular Function", false),
    // Subset of GO: Biological Process
    GO_BP("biological_process", "Gene Ontology: Biological Process", false),
    // PATO
    QUALITY("quality", "Phenotypic Quality Ontology", false),
    // PATO: Processes
    // this is "Process Quality"
    QUALITY_PROCESSES("quality.process", "quality", "Phenotype and Trait Ontology: Quality of Process", false),
    // PATO: Processes
    QUALITY_PROCESSES_RELATIONAL("pato.eq.quality.process.relational", "quality", "Phenotype and Trait Ontology: Relation of Process", false),
    // PATO: Qualities
    // this is "physical object quality"
    QUALITY_QUALITIES("quality.quality", "quality", "Phenotype and Trait Ontology: Quality of Qualities", false),
    // PATO: Qualitative
    // This is "qualitative"
    QUALITY_QUALITATIVE("pato.eq.quality.qualitative", "Phenotype and Trait Ontology: Quality of Qualities", false),
    // BP and MF
    // PATO: Processes
    QUALITY_OBJECT_RELATIONAL("pato.eq.quality.object.relational", "Phenotype and Trait Ontology: Relation of Object", false),
    GO_BP_MF(GO_BP.getOntologyName() + "," + GO_MF.getOntologyName(), "Gene Ontology: Biological Process and Molecular Function", true),
    // Spatial
    SPATIAL("spatial", "Spatial Ontology", false),
    BEHAVIOR("behavior_ontology", "Behavior Ontology", false),
    MPATH("mouse_pathology.ontology", "MPATH", false),
    MPATH_NEOPLASM("mpath_neoplasm", "mouse_pathology.ontology", "Mouse Cancer Pathology Ontology-Neoplasm Branch", false);

    private String ontologyName;
    private String commonName;
    private String dbOntologyName;
    private boolean composedOntologies;

    private Ontology(String name, String commonName, boolean composed) {
        this.ontologyName = name;
        this.commonName = commonName;
        this.composedOntologies = composed;
        this.dbOntologyName = ontologyName;
    }

    private Ontology(String name, String dbOntologyName, String commonName, boolean composed) {
        this.ontologyName = name;
        this.commonName = commonName;
        this.composedOntologies = composed;
        this.dbOntologyName = dbOntologyName;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public boolean isComposedOntologies() {
        return composedOntologies;
    }

    public String toString() {
        return ontologyName;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getDbOntologyName() {
        return dbOntologyName;
    }


    public static Ontology getOntology(String name) {
        if (name == null)
            return null;
        for (Ontology ontology : values()) {
            if (ontology.getOntologyName().equals(name))
                return ontology;
        }
        return null;
    }

    public static Ontology[] getSerializableOntologies() {
        Ontology[] ontologies = new Ontology[12];
        int i = 0;
        ontologies[i++] = Ontology.STAGE;
        ontologies[i++] = Ontology.ANATOMY;
        ontologies[i++] = Ontology.QUALITY;
        ontologies[i++] = Ontology.QUALITY_PROCESSES;
        ontologies[i++] = Ontology.QUALITY_QUALITIES;
        ontologies[i++] = Ontology.GO_CC;
        ontologies[i++] = Ontology.GO_MF;
        ontologies[i++] = Ontology.GO_BP;
        ontologies[i++] = Ontology.BEHAVIOR;
        ontologies[i++] = Ontology.MPATH;
        ontologies[i++] = Ontology.MPATH_NEOPLASM;
        ontologies[i] = Ontology.SPATIAL;

        return ontologies;
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
     *
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
        for (String ontName : ontNames) {
            ontologies.add(getOntology(ontName));
        }
        return ontologies;
    }

    /**
     * Retrieve a collection of ontologies that are sub-ontologies to a given root.
     *
     * @param rootOntology root ontology
     * @return collection of ontologies.
     */
    public static Collection<Ontology> getSubOntologies(Ontology rootOntology) {
        Set<Ontology> subOntologies = new HashSet<Ontology>(3);
        String rootOntologyName = rootOntology.ontologyName;
        for (Ontology ontology : values()) {
            if (ontology != rootOntology && ontology.getOntologyName().startsWith(rootOntologyName))
                subOntologies.add(ontology);
        }
        return subOntologies;
    }

    public int compareOntologyTo(Ontology o) {
        if (o == null)
            return +1;

        return this.getOntologyName().compareTo(o.getOntologyName());
    }

    public static boolean isSpatial(Ontology ontology) {
        if (ontology == null)
            return false;
        if (ontology.equals(SPATIAL))
            return true;
        return false;
    }

    /**
     * Retrieve the root ontology if the existing ontology is a slim.
     *
     * @return Ontology
     */
    public Ontology getRootOntology() {
        Ontology rootOntology = getOntology(dbOntologyName);
        if (rootOntology == null)
            return this;
        else
            return rootOntology;
    }

    static Set<Ontology> doNotIndexOntologyList = new HashSet<Ontology>(5);

    static {
        doNotIndexOntologyList.add(Ontology.STAGE);
        doNotIndexOntologyList.add(Ontology.GO);
        doNotIndexOntologyList.add(Ontology.GO_BP_MF);
        doNotIndexOntologyList.add(Ontology.QUALITY_OBJECT_RELATIONAL);
        doNotIndexOntologyList.add(Ontology.QUALITY_PROCESSES);
        doNotIndexOntologyList.add(Ontology.QUALITY_PROCESSES_RELATIONAL);
        doNotIndexOntologyList.add(Ontology.QUALITY_QUALITATIVE);
        doNotIndexOntologyList.add(Ontology.QUALITY_QUALITIES);
        doNotIndexOntologyList.add(Ontology.ANATOMY_FULL);
        doNotIndexOntologyList.add(Ontology.QUALITY_QUALITIES);
    }

    public boolean shouldNotBeIndexed() {
        return doNotIndexOntologyList.contains(this);
    }
}
