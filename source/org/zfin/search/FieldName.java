package org.zfin.search;

import org.zfin.ontology.Ontology;

/**
 * This enumeration lists all field names used in SOLR
 */
public enum FieldName {

    AFFECTED_ANATOMY_TF("affected_anatomy_tf"),
    AFFECTED_BIOLOGICAL_PROCESS_TF("affected_biological_process_tf"),
    AFFECTED_MOLECULAR_FUNCTION_TF("affected_molecular_function_tf"),
    AFFECTED_CELLULAR_COMPONENT_TF("affected_cellular_component_tf"),
    AFFECTED_GENE("affected_gene"),
    ANATOMY_TF("anatomy_tf"),
    ANTIGEN_GENE("antigen_gene"),
    AUTHOR("registered_author"),
    ASSAY("assay"),
    BACKGROUND("background"),
    BIOLOGICAL_PROCESS_TF("biological_process_tf"),
    CELLULAR_COMPONENT_TF("cellular_component_tf"),
    CHROMOSOME("chromosome", "Location"),
    CODING_SEQUENCE("coding_sequence"),
    CONSTRUCT("construct"),
    DATE("date"),
    DISEASE_MODEL("disease_model"),
    ENGINEERED_REGION("engineered_region"),
    EXPERIMENTAL_CONDITIONS("experimental_conditions"),
    EXPRESSIONS_ANATOMY_TF("expression_anatomy_tf"),
    EXPRESSED_IN_TF("expressed_in_tf"),
    GENE("gene"),
    GENES_WITH_ALTERED_EXPRESSION("genes_with_altered_expression"),
    GENOTYPE("genotype"),
    HAS_IMAGE("has_image"),
    HOST_ORGANISM("host_organism"),
    INSERTED_IN_GENE("inserted_in_gene"),
    INSTITUTION("institution"),
    IS_GENOTYPE_WILDTYPE("is_genotype_wildtype","Genotype"),
    IS_MONOGENIC("is_monogenic"),
    IS_WILDTYPE("is_wildtype"),
    JOURNAL("journal"),
    KEYWORD("keyword"),
    LAB_OF_ORIGIN("lab_of_origin"),
    LABELED_STRUCTURE_TF("labeled_structure_tf"),
    MOLECULAR_FUNCTION_TF("molecular_function_tf"),
    OBSOLETE("obsolete"),
    ONTOLOGY("ontology"),
    PHENOTYPE_QUALITY_TF("phenotype_quality_tf"),
    PHENOTYPE_STATEMENT("phenotype_statement"),
    REGULATORY_REGION("regulatory_region"),
    PUBLICATION_TYPE("publication_type"),
    REPORTER_COLOR("reporter_color"),
    REPORTER_EXPRESSION_ANATOMY_TF("reporter_expression_anatomy_tf", "Expressed In"),
    REPORTER_GENE("reporter_gene"),
    SEQUENCE_ALTERATION("sequence_alteration", "Mutation / Tg"),
//    SCREEN("screen"),   <!--screen used to be here, removed as a result of case 11323-->
    MUTAGEN("mutagen"),
    SEQUENCE_TARGETING_REAGENT("sequence_targeting_reagent"),
    SOURCE("source"),
    STAGE("stage"),
    TARGETED_GENE("targeted_gene"),
    TOPIC("topic"),
    STATUS("pub_status"),
    TYPE("type"),
    ZEBRAFISH_GENE("zebrafish_gene"),;

    private String name;
    private String prettyName;

    FieldName(String name) {
        this.name = name;
    }

    FieldName(String name, String prettyName) {
        this.name = name;
        this.prettyName = prettyName;
    }

    public String getName() {
        return name;
    }

    public boolean isTermFacet() {
        return name.endsWith("tf");
    }

    public static FieldName getFieldName(String name) {
        if (name == null)
            return null;
        for (FieldName fieldName : values()) {
            if (fieldName.getName().equals(name))
                return fieldName;
        }
        return null;
    }

    public String getPrettyName() {
        if (prettyName != null)
            return prettyName;
        return name;
    }

    public boolean hasDefinedPrettyName() {
        return prettyName != null;
    }

    public static String getFieldName(Ontology ontology) {
        for (FieldName fNname : values())
            if (fNname.getName().startsWith(ontology.getDbOntologyName()))
                return fNname.getName();
        return null;
    }

    public static String getAffectedFieldName(Ontology ontology) {
        for (FieldName fNname : values())
            if (fNname.getName().startsWith("affected_" + ontology.getDbOntologyName()) ||
                    fNname.getName().startsWith("affected_anatomy"))
                return fNname.getName();
        return null;
    }
}
