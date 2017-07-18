package org.zfin.search;

import org.apache.commons.lang.StringUtils;
import org.zfin.ontology.Ontology;

import java.util.ArrayList;
import java.util.List;

/**
 * This enumeration lists all field names used in SOLR
 */
public enum FieldName {

    AFFECTED_ANATOMY("affected_anatomy"),
    AFFECTED_ANATOMY_TF("affected_anatomy_tf"),
    AFFECTED_BIOLOGICAL_PROCESS("affected_biological_process"),
    AFFECTED_BIOLOGICAL_PROCESS_TF("affected_biological_process_tf"),
    AFFECTED_CELLULAR_COMPONENT("affected_cellular_component"),
    AFFECTED_CELLULAR_COMPONENT_TF("affected_cellular_component_tf"),
    AFFECTED_GENE("affected_gene"),
    AFFECTED_MOLECULAR_FUNCTION("affected_molecular_function"),
    AFFECTED_MOLECULAR_FUNCTION_TF("affected_molecular_function_tf"),
    ALIAS_KEYWORD("alias_k"),
    ALIAS("alias", "synonym"),
    ALIAS_AC("alias_ac", "synonym"),
    ANATOMY("anatomy"),
    ANATOMY_TF("anatomy_tf"),
    ANTIBODY_TYPE("antibody_type","Type"),
    ANTIGEN_GENE("antigen_gene"),
    ASSAY("assay"),
    AUTHOR("registered_author"),
    AUTHOR_STRING("author_string","Author List"),
    BACKGROUND("background"),
    BIOLOGICAL_PROCESS("biological_process"),
    BIOLOGICAL_PROCESS_TF("biological_process_tf"),
    CATEGORY("category"),
    CELLULAR_COMPONENT("cellular_component"),
    CELLULAR_COMPONENT_TF("cellular_component_tf"),
    CHROMOSOME("chromosome", "Location"),
    CODING_SEQUENCE("coding_sequence"),
    CONDITIONS("conditions"),
    CONSEQUENCE("rna_consequence"),
    CONSTRUCT("construct"),
    CURATION_STATUS("curation_status"),
    CURATION_LOCATION("location"),
    DATE("date"),
    DISEASE("disease", "Human Disease"),
    DISEASE_MODEL("disease_model"),
    ENGINEERED_REGION("engineered_region"),
    EXPRESSED_IN_TF("expressed_in_tf"),
    EXPRESSION_ANATOMY_TF("expression_anatomy_tf"),
    EXPRESSION_ANATOMY("expression_anatomy"),
    FIGURE_ID("figure_id"),
    FISH("fish"),
    FULL_NAME("full_name", "name"),
    FULL_NAME_AC("full_name_ac", "name"),
    GENE("gene"),
    GENE_FULL_NAME("gene_full_name"),
    GENE_PREVIOUS_NAME("gene_previous_name"),
    GENES_WITH_ALTERED_EXPRESSION("genes_with_altered_expression"),
    GENOTYPE("genotype"),
    GENOTYPE_FULL_NAME("genotype_full_name"),
    HAS_IMAGE("has_image"),
    HOST_ORGANISM("host_organism"),
    ID("id"),
    IMG_ZDB_ID("img_zdb_id"),
    INSERTED_IN_GENE("inserted_in_gene"),
    INSTITUTION("institution"),
    IS_GENOTYPE_WILDTYPE("is_genotype_wildtype","Genotype"),
    IS_MONOGENIC("is_monogenic"),
    IS_WILDTYPE("is_wildtype"),
    JOURNAL("journal"),
    KEYWORD("keyword"),
    LAB_OF_ORIGIN("lab_of_origin"),
    LABELED_STRUCTURE_TF("labeled_structure_tf"),
    MESH_TERM("full_mesh_term","MeSH Term"),
    MESH_TERM_TEXT("full_mesh_term_t","MeSH Term"),
    MISEXPRESSED_GENE("misexpressed_gene"),
    MOLECULAR_FUNCTION("molecular_function"),
    MOLECULAR_FUNCTION_TF("molecular_function_tf"),
    MUTAGEN("mutagen"),
    MUTATION_TYPE("mutation_type"),
    NAME("name"),
    NAME_SORT("name_sort"),
    NOTE("note"),
    ONTOLOGY("ontology"),
    PHENOTYPE_QUALITY_TF("phenotype_quality_tf"),
    PHENOTYPE_STATEMENT("phenotype_statement"),
    PROPER_NAME("proper_name"),
    PUBLICATION_STATUS("publication_status"),
    PUBLICATION_TYPE("publication_type"),
    PUB_OWNER("owner"),
    REGULATORY_REGION("regulatory_region"),
    RELATED_ACCESSION("related_accession", "Accession"),
    RELATED_ACCESSION_TEXT("related_accession_t", "Accession"),
    REPORTER_COLOR("reporter_color"),
    REPORTER_EXPRESSION_ANATOMY_TF("reporter_expression_anatomy_tf", "Expressed In"),
    REPORTER_GENE("reporter_gene"),
    SCREEN("screen"),
    SEQUENCE_ALTERATION("sequence_alteration", "Mutation / Tg"),
    SEQUENCE_TARGETING_REAGENT("sequence_targeting_reagent"),
    SOURCE("source"),
    STAGE("stage"),
    STATUS("pub_status"),
    TARGET("target"),
    TARGET_FULL_NAME("target_full_name"),
    TARGET_PREVIOUS_NAME("target_previous_name"),
    TERM_STATUS("term_status"),
    THUMBNAIL("thumbnail"),
    TOPIC("topic"),
    TYPE("type"),
    TYPE_TREE("type", 3, "Type"),
    TYPEGROUP("typegroup"),
    URL("url"),
    XREF("xref"),
    ZEBRAFISH_GENE("zebrafish_gene");

    private String name;
    private String prettyName;
    private Integer depth;

    FieldName(String name) {
        this.name = name;
        this.depth = 1;
    }

    FieldName(String name, String prettyName) {
        this.name = name;
        this.prettyName = prettyName;
        this.depth = 1;
    }

    FieldName(String name, Integer depth) {
        this.name = name;
        this.depth = depth;
    }

    FieldName(String name, Integer depth, String prettyName) {
        this.name = name;
        this.depth = depth;
        this.prettyName = prettyName;
    }

    public String getName() {
        return name;
    }
    public String toString() { return name; }

    public boolean isTermFacet() {
        return name.endsWith("tf");
    }
    public boolean isHierarchical() { return (depth > 1); }


    public static FieldName getFieldName(String name) {
        if (name == null)
            return null;
        for (FieldName fieldName : values()) {
            if (fieldName.getName().equals(name))
                return fieldName;
        }
        return null;
    }

    public String getPivotKey() {
        List<String> fields = new ArrayList<>();
        for (int i = 0 ; i < depth ; i++) {
            fields.add(getName() + "_" + i);
        }
        return StringUtils.join(fields, ",");
    }

    public String getPrettyName() {
        if (prettyName != null)
            return prettyName;
        return name;
    }

    public boolean hasDefinedPrettyName() {
        return prettyName != null;
    }

    public static FieldName getFieldName(Ontology ontology) {
        for (FieldName fNname : values())
            if (fNname.getName().startsWith(ontology.getDbOntologyName()))
                return fNname;
        return null;
    }

    public static FieldName getAffectedFieldName(Ontology ontology) {
        for (FieldName fNname : values())
            if (fNname.getName().startsWith("affected_" + ontology.getDbOntologyName()) ||
                    fNname.getName().startsWith("affected_anatomy"))
                return fNname;
        return null;
    }
}
