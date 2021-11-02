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
    ALIAS("alias", "synonym"),
    ALIAS_AC("alias_ac", "synonym"),
    ALIAS_KEYWORD("alias_k"),
    ALIAS_KEYWORD_AUTOCOMPLETE("alias_kac", "synonym"),
    ALIAS_T("alias_t"),
    ANATOMY("anatomy"),
    ANATOMY_TF("anatomy_tf"),
    ANTIBODY_TYPE("antibody_type","Type"),
    ANTIGEN_GENE("antigen_gene"),
    ANY_COLOR("any_color"),
    ASSAY("assay"),
    AUTHOR("registered_author"),
    AUTHOR_SORT("author_sort"),
    AUTHOR_STRING("author_string","Author List"),
    BACKGROUND("background"),
    BIOLOGICAL_PROCESS("biological_process"),
    BIOLOGICAL_PROCESS_TF("biological_process_tf"),
    CATEGORY("category"),
    CELLULAR_COMPONENT("cellular_component"),
    CELLULAR_COMPONENT_TF("cellular_component_tf"),
    CHROMOSOME("chromosome", "Location"),
    CLONE_AUTOCOMPLETE("clone_ac", "Clone"),
    CODING_SEQUENCE("coding_sequence"),
    CODING_SEQUENCE_AC("coding_sequence_ac"),
    CODING_SEQUENCE_SORT("coding_sequence_sort"),
    CODING_SEQUENCE_SPECIES("coding_sequence_species"),
    CONDITIONS("conditions"),
    CONSEQUENCE("rna_consequence"),
    CONSTRUCT("construct"),
    CONSTRUCT_CITATION_SORT("construct_citation_sort"),
    CONSTRUCT_NAME("construct_name"),
    CONSTRUCT_SPECIES_SORT("construct_species_sort"),
    COUNTRY("country"),
    CURATION_LOCATION("location"),
    CURATION_STATUS("curation_status"),
    CURATOR("curator"),
    DATE("date"),
    DISEASE("disease", "Human Disease"),
    DISEASE_MODEL("disease_model"),
    EMISSION_COLOR("emission_color"),
    EXCITATION_COLOR("excitation_color"),
    ENGINEERED_REGION("engineered_region"),
    EVIDENCE_CODE("evidence_code"),
    EXPRESSED_GENE_FULL_NAME("expressed_gene_full_name"),
    EXPRESSED_GENE_PREVIOUS_NAME("expressed_gene_previous_name"),
    EXPRESSED_IN_TF("expressed_in_tf"),
    EXPRESSION_ANATOMY("expression_anatomy"),
    EXPRESSION_ANATOMY_DIRECT("expression_anatomy_direct"),
    EXPRESSION_ANATOMY_RELATED_BY_GENE_AND_EXPERIMENT_DIRECT("expression_anatomy_related_by_gene_and_experiment_direct"),
    EXPRESSION_ANATOMY_RELATED_BY_GENE_AND_EXPERIMENT_PARENT("expression_anatomy_related_by_gene_and_experiment_parent"),
    EXPRESSION_ANATOMY_TF("expression_anatomy_tf"),
    FIG_ZDB_ID("fig_zdb_id"),
    FIGURE_ID("figure_id"),
    FISH("fish"),
    FISH_T("fish_t"),
    FISH_ZDB_ID("fish_zdb_id"),
    FULL_NAME("full_name", "Name"),
    FULL_NAME_AC("full_name_ac", "Name"),
    FULL_NAME_KEYWORD_AUTOCOMPLETE("full_name_kac", "Name"),
    GENE("gene"),
    GENE_AUTOCOMPLETE("gene_ac"),
    GENE_FULL_NAME("gene_full_name"),
    GENE_FULL_NAME_AUTOCOMPLETE("gene_full_name_ac", "Gene Full Name"),
    GENE_PREVIOUS_NAME("gene_previous_name"),
    GENE_PREVIOUS_NAME_AUTOCOMPLETE("gene_previous_name_ac","Gene Previous Name"),
    GENE_PREVIOUS_NAME_KEYWORD_AUTOCOMPLETE("gene_previous_name_kac","Gene Previous Name"),
    GENE_SORT("gene_sort"),
    GENE_ZDB_ID("gene_zdb_id"),
    GENES_WITH_ALTERED_EXPRESSION("genes_with_altered_expression"),
    GENOTYPE("genotype"),
    GENOTYPE_FULL_NAME("genotype_full_name"),
    GROUP_KEY("group_key"),
    HAS_IMAGE("has_image"),
    HOST_ORGANISM("host_organism"),
    ID("id"),
    ID_T("id_t"),
    IMG_ZDB_ID("img_zdb_id"),
    INSERTED_IN_GENE("inserted_in_gene"),
    INSTITUTION("institution"),
    IS_GENOTYPE_WILDTYPE("is_genotype_wildtype","Genotype"),
    IS_MONOGENIC("is_monogenic"),
    IS_WILDTYPE("is_wildtype"),
    IS_ZEBRASHARE("is_zebrashare"),
    JOURNAL("journal"),
    JOURNAL_NAME("journal_name"),
    JOURNAL_NAME_T("journal_name_t"),
    JOURNAL_T("journal_t"),
    JOURNAL_TYPE("journal_type"),
    KEYWORD("keyword"),
    KEYWORD_AC("keyword_ac"),
    LAB_OF_ORIGIN("lab_of_origin"),
    LABELED_STRUCTURE_TF("labeled_structure_tf"),
    MESH_TERM("full_mesh_term","MeSH Term"),
    MESH_TERM_TEXT("full_mesh_term_t","MeSH Term"),
    MISEXPRESSED_GENE("misexpressed_gene"),
    MOLECULAR_FUNCTION("molecular_function"),
    MOLECULAR_FUNCTION_TF("molecular_function_tf"),
    MONOGENIC_GENE_ZDB_ID("monogenic_gene_zdb_id"),
    MUTAGEN("mutagen"),
    MUTATION_TYPE("mutation_type"),
    NAME("name","Abbreviation"),
    NAME_AC("name_ac","Abbreviation"),
    NAME_SORT("name_sort"),
    NOTE("note"),
    ONTOLOGY("ontology"),
    ORTHOLOG_OTHER_SPECIES_NAME("ortholog_other_species_name","Orthologue"),
    ORTHOLOG_OTHER_SPECIES_NAME_AUTOCOMPLETE("ortholog_other_species_name_ac","Orthologue"),
    ORTHOLOG_OTHER_SPECIES_NAME_KEYWORD_AUTOCOMPLETE("ortholog_other_species_name_kac","Orthologue"),
    ORTHOLOG_OTHER_SPECIES_SYMBOL("ortholog_other_species_symbol","Orthologue"),
    ORTHOLOG_OTHER_SPECIES_SYMBOL_AUTOCOMPLETE("ortholog_other_species_symbol_ac","Orthologue"),
    ORTHOLOG_OTHER_SPECIES_SYMBOL_KEYWORD_AUTOCOMPLETE("ortholog_other_species_symbol_kac","Orthologue"),
    PAGES("pages"),
    PET_DATE("pet_date"),
    PHENOTYPE_QUALITY_TF("phenotype_quality_tf"),
    PHENOTYPE_STATEMENT("phenotype_statement"),
    PROBE("probe"),
    PROPER_NAME("proper_name"),
    PUB_OWNER("owner"),
    PUB_SIMPLE_STATUS("pub_simple_status"),
    PUB_ZDB_ID("pub_zdb_id"),
    PUBLICATION("publication"),
    PUBLICATION_STATUS("publication_status"),
    PUBLICATION_TYPE("publication_type"),
    QUALIFIER("qualifier"),
    REGISTERED_AUTHOR_AUTOCOMPLETE("registered_author_ac"),
    REGULATORY_REGION("regulatory_region"),
    REGULATORY_REGION_AC("regulatory_region_ac"),
    REGULATORY_REGION_SORT("regulatory_region_sort"),
    REGULATORY_REGION_SPECIES("regulatory_region_species"),
    RELATED_ACCESSION("related_accession", "Accession"),
    RELATED_ACCESSION_TEXT("related_accession_t", "Accession"),
    RELATED_GENE_SYMBOL("related_gene_symbol"),
    RELATED_GENE_ZDB_ID("related_gene_zdb_id"),
    RELATED_SPECIES_NAME_AC("related_species_name_ac"),
    REPORTER_COLOR("reporter_color"),
    REPORTER_EXPRESSION_ANATOMY_TF("reporter_expression_anatomy_tf", "Expressed In"),
    REPORTER_GENE("reporter_gene"),
    REPORTER_GENE_T("reporter_gene_t"),
    SEQUENCE_ALTERATION("sequence_alteration", "Mutation / Tg"),
    SEQUENCE_TARGETING_REAGENT("sequence_targeting_reagent"),
    SOURCE("source"),
    STAGE("stage"),
    STAGE_HOURS_END("stage_hours_end"),
    STAGE_HOURS_START("stage_hours_start"),
    TARGET("target"),
    TARGET_FULL_NAME("target_full_name"),
    TARGET_FULL_NAME_AUTOCOMPLETE("target_full_name_ac", "Target Full Name"),
    TARGET_FULL_NAME_KEYWORD_AUTOCOMPLETE("target_full_name_ac", "Target Full Name"),
    TARGET_PREVIOUS_NAME("target_previous_name"),
    TARGET_PREVIOUS_NAME_AUTOCOMPLETE("target_previous_name_ac", "Target Previous Name"),
    TARGET_PREVIOUS_NAME_KEYWORD_AUTOCOMPLETE("target_previous_name_ac", "Target Previous Name"),
    TERM_ONTOLOGY("term_ontology"),
    TERM_STATUS("term_status"),
    THUMBNAIL("thumbnail"),
    TOPIC("topic"),
    TYPE("type"),
    TYPE_TREE("type", 3, "Type"),
    TYPEGROUP("typegroup"),
    URL("url"),
    VOLUME("volume"),
    XPATRES_ID("xpatres_id"),
    XREF("xref"),
    YEAR("year"),
    ZEBRAFISH_GENE("zebrafish_gene"),
    ZEBRAFISH_GENE_T("zebrafish_gene_t");

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
        for (FieldName fNname : values()) {
            if (fNname.getName().startsWith("affected_" + ontology.getDbOntologyName())) {
                return fNname;
            } else if (ontology.isAnatomy(ontology) && fNname.getName().startsWith("affected_anatomy")) {
                return fNname;
            }
        }
        return null;
    }
}
