package org.zfin.database.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ForeignKey {

    ANATOMY_AO_DISPLAY(Table.ANATOMY_ITEM, Table.ANATOMY_DISPLAY, "anatdisp_item_zdb_id"),
    APATO_TAG_PHENOS(Table.APATO_TAG, Table.PHENOTYPE_STATEMENT, "phenos_tag"),
    TERM_AO_STATS(Table.ANATOMY_STATS, Table.TERM, "anatstat_term_zdb_id"),
    STAGE_AO_DISPLAY(Table.STAGE, Table.ANATOMY_DISPLAY, "anatdisp_stg_zdb_id"),
    START_STAGE_ANATOMY_ITEM(Table.STAGE, Table.ANATOMY_ITEM, "anatitem_start_stg_zdb_id"),
    END_STAGE_ANATOMY_ITEM(Table.STAGE, Table.ANATOMY_ITEM, "anatitem_end_stg_zdb_id"),
    GENOX_GENO(Table.GENOTYPE, Table.GENOTYPE_EXPERIMENT, "genox_geno_zdb_id"),
    GENOX_EXP(Table.EXPERIMENT, Table.GENOTYPE_EXPERIMENT, "genox_exp_zdb_id"),
    EXPCOND_EXP(Table.EXPERIMENT, Table.EXPERIMENT_CONDITION, "expcond_exp_zdb_id"),
    EXPCOND_CDT(Table.CONDITION_DATA_TYPE, Table.EXPERIMENT_CONDITION, "expcond_cdt_zdb_id"),
    XPATEX_GENOX(Table.GENOTYPE_EXPERIMENT, Table.EXPRESSION_EXPERIMENT, "xpatex_genox_zdb_id"),
    PHENOX_GENOX(Table.GENOTYPE_EXPERIMENT, Table.PHENOTYPE_EXPERIMENT, "phenox_genox_zdb_id"),
    PHENOS_PHENOX(Table.PHENOTYPE_EXPERIMENT, Table.PHENOTYPE_STATEMENT, "phenos_phenox_pk_id"),
    PHENOX_TERM_1_A(Table.TERM, Table.PHENOTYPE_STATEMENT, "phenos_entity_1_superterm_zdb_id"),
    PHENOX_TERM_2_A(Table.TERM, Table.PHENOTYPE_STATEMENT, "phenos_entity_2_superterm_zdb_id"),
    PHENOS_TERM_1_B(Table.TERM, Table.PHENOTYPE_STATEMENT, "phenos_entity_1_subterm_zdb_id"),
    PHENOS_TERM_2_B(Table.TERM, Table.PHENOTYPE_STATEMENT, "phenos_entity_2_subterm_zdb_id"),
    XPATRES_START_STAGE(Table.STAGE, Table.EXPRESSION_RESULT, "xpatres_start_stg_zdb_id"),
    XPATRES_END_STAGE(Table.STAGE, Table.EXPRESSION_RESULT, "xpatres_end_stg_zdb_id"),
    XPAT_TERM_1(Table.TERM, Table.EXPRESSION_RESULT, "xpatres_superterm_zdb_id"),
    XPAT_TERM_2(Table.TERM, Table.EXPRESSION_RESULT, "xpatres_subterm_zdb_id"),
    ALL_TERM_PARENT_TERM(Table.TERM, Table.ALL_TERM_CONTAINS, "alltermcon_container_zdb_id"),
    ALL_TERM_CHILD_TERM(Table.TERM, Table.ALL_TERM_CONTAINS, "alltermcon_contained_zdb_id"),
    PHENOS_QUALITY_TERM(Table.TERM, Table.PHENOTYPE_STATEMENT, "phenos_quality_zdb_id"),
    RELATIONSHIP_TERM_1_TERM(Table.TERM, Table.TERM_RELATIONSHIP, "termrel_term_1_zdb_id"),
    RELATIONSHIP_TERM_2_TERM(Table.TERM, Table.TERM_RELATIONSHIP, "termrel_term_2_zdb_id"),
    RELATIONSHIP_TYPE(Table.TERM_RELATIONSHIP_TYPE, Table.TERM_RELATIONSHIP, "termrel_type"),
    TERM_STATS_TERM(Table.TERM, Table.TERM_STATS, "anatstat_term_zdb_id"),
    TERM_OBSOLETE_TERM_REPLACE(Table.TERM, Table.OBSOLETE_TERM_REPLACEMENT, "obstermrep_term_zdb_id"),
    REPLACED_TERM_OBSOLETE_TERM_REPLACE(Table.TERM, Table.OBSOLETE_TERM_REPLACEMENT, "obstermrep_term_replacement_zdb_id"),
    SUGGESTION_TERM_OBSOLETE_TERM_SUGGESTION(Table.TERM, Table.OBSOLETE_TERM_SUGGESTION, "obstermsug_term_suggestion_zdb_id"),
    TERM_OBSOLETE_TERM_SUGGESTION(Table.TERM, Table.OBSOLETE_TERM_SUGGESTION, "obstermsug_term_zdb_id"),
    PHENOX_FIG(Table.FIGURE, Table.PHENOTYPE_EXPERIMENT, "phenox_fig_zdb_id"),
    FIG_PUB(Table.PUBLICATION, Table.FIGURE, "fig_source_zdb_id"),
    PHENOX_START_STAGE(Table.STAGE, Table.PHENOTYPE_EXPERIMENT, "phenox_start_stg_zdb_id"),
    PHENOX_END_STAGE(Table.STAGE, Table.PHENOTYPE_EXPERIMENT, "phenox_end_stg_zdb_id"),
    XPATRES_XPAT(Table.EXPRESSION_EXPERIMENT, Table.EXPRESSION_RESULT, "xpatres_xpatex_zdb_id"),
    FIGURE_XPATRES_ASSOC(Table.FIGURE, Table.EXPRESSION_RESULT, Table.FIGURE_XPATRES),
    XPATRES_FIGURE_ASSOC(Table.EXPRESSION_RESULT, Table.FIGURE, Table.XPATRES_FIGURE),
    MUTAGEN_FEATURE_ASSAY(Table.MUTAGEN, Table.FEATURE_ASSAY, "featassay_mutagen"),
    MUTAGEE_FEATURE_ASSAY(Table.MUTAGEE, Table.FEATURE_ASSAY, "featassay_mutagee"),
    FEATURE_FEATURE_ASSAY(Table.FEATURE, Table.FEATURE_ASSAY, "featassay_feature_zdb_id"),
    FEATURE_FEATURE_HISTORY(Table.FEATURE, Table.FEATURE_HISTORY, "fhist_ftr_zdb_id"),
    FEATURE_EVENT_FEATURE_HISTORY(Table.MARKER_HISTORY_EVENT, Table.FEATURE_HISTORY, "fhist_event"),
    FEATURE_REASON_FEATURE_HISTORY(Table.MARKER_HISTORY_REASON, Table.FEATURE_HISTORY, "fhist_reason"),
    ALIAS_FEATURE_HISTORY(Table.DATA_ALIAS, Table.FEATURE_HISTORY, "fhist_dalias_zdb_id"),
    PERSON_LAB_ASSOC(Table.PERSON, Table.LAB, Table.PERSON_LAB),
    LAB_PERSON_ASSOC(Table.LAB, Table.PERSON, Table.LAB_PERSON),
    PERSON_PUB_ASSOC(Table.PERSON, Table.PUBLICATION, Table.PERSON_PUB),
    PUB_PERSON_ASSOC(Table.PUBLICATION, Table.PERSON, Table.PUB_PERSON),
    PERSON_COMP_ASSOC(Table.PERSON, Table.COMPANY, Table.PERSON_COMP),
    COMP_PERSON_ASSOC(Table.COMPANY, Table.PERSON, Table.COMP_PERSON),
    TERM_IMAGE_ASSOC(Table.TERM, Table.IMAGE, Table.TERM_IMAGE),
    IMAGE_TERM_ASSOC(Table.IMAGE, Table.TERM, Table.IMAGE_TERM),
    MARKER_LAB_ASSOC(Table.MARKER, Table.LAB, Table.MARKER_LAB),
    MARKER_COMP_ASSOC(Table.MARKER, Table.COMPANY, Table.MARKER_COMP),
    GENO_LAB_ASSOC(Table.GENOTYPE, Table.LAB, Table.GENO_LAB),
    GENO_COMP_ASSOC(Table.GENOTYPE, Table.COMPANY, Table.GENO_COMP),
    FEATURE_LAB_ASSOC(Table.FEATURE, Table.LAB, Table.FEATURE_LAB),
    FEATURE_COMP_ASSOC(Table.FEATURE, Table.COMPANY, Table.FEATURE_COMP),
    LAB_MARKER_ASSOC(Table.LAB, Table.MARKER, Table.LAB_MARKER),
    COMP_MARKER_ASSOC(Table.COMPANY, Table.MARKER, Table.COMP_MARKER),
    LAB_GENO_ASSOC(Table.LAB, Table.GENOTYPE, Table.LAB_GENO),
    COMP_GENO_ASSOC(Table.COMPANY, Table.GENOTYPE, Table.COMP_GENO),
    LAB_FEATURE_ASSOC(Table.LAB, Table.FEATURE, Table.LAB_FEATURE),
    COMP_FEATURE_ASSOC(Table.COMPANY, Table.FEATURE, Table.COMP_FEATURE),
    PERSON_PERSON_LAB(Table.PERSON, Table.INT_PERSON_LAB, "source_id"),
    LAB_PERSON_LAB(Table.LAB, Table.INT_PERSON_LAB, "target_id"),
    PERSON_PERSON_PUB(Table.PERSON, Table.INT_PERSON_PUB, "source_id"),
    PUB_PERSON_PUB(Table.PUBLICATION, Table.INT_PERSON_PUB, "target_id"),
    PERSON_PERSON_COMP(Table.PERSON, Table.INT_PERSON_COMPANY, "source_id"),
    COMP_PERSON_COMP(Table.COMPANY, Table.INT_PERSON_COMPANY, "target_id"),
    OWNER_LAB(Table.PERSON, Table.LAB, "owner"),
    OWNER_COMP(Table.PERSON, Table.COMPANY, "owner"),
    CONTACT_PERSON_LAB(Table.PERSON, Table.LAB, "contact_person"),
    CONTACT_PERSON_COMP(Table.PERSON, Table.COMPANY, "contact_person"),
    BLAST_TYPE_BLAST_DB(Table.BLAST_DB_TYPE, Table.BLAST_DB, "blastdb_type"),
    BLAST_ORINGINATION_BLAST_DB(Table.BLAST_DB_ORINGINATION_TYPE, Table.BLAST_DB, "blastdb_origination_id"),
    BLAST_QUERY_BLAST_DB(Table.BLAST_QUERY, Table.BLAST_HIT, "bhit_bqry_zdb_id"),
    ACCESSION_BLAST_DB(Table.ACCESSION_BANK, Table.BLAST_HIT, "bhit_target_accbk_pk_id"),
    RUN_CANDIDATE_BLAST_QUERY(Table.RUN_CANDIDATE, Table.BLAST_QUERY, "bqry_runcan_zdb_id"),
    ACCESSION_BLAST_QUERY(Table.ACCESSION_BANK, Table.BLAST_QUERY, "bqry_accbk_pk_id"),
    BLAST_QUERY_BLAST_REPORT(Table.BLAST_QUERY, Table.BLAST_REPORT, "brpt_bqry_zdb_id"),
    BLAST_DB_PARENT_BLAST_ORDER(Table.BLAST_DB, Table.BLASTDB_ORDER, "bdborder_parent_blastdb_zdb_id"),
    BLAST_DB_CHILD_BLAST_ORDER(Table.BLAST_DB, Table.BLASTDB_ORDER, "bdborder_child_blastdb_zdb_id"),
    BLAST_DB_BLAST_REGEN(Table.BLAST_DB, Table.BLASTDB_REGEN_CONTENT, "brc_blastdb_zdb_id"),

    FEATURE_RELATION_FEATURE(Table.FEATURE, Table.FEATURE_RELATION, "fmrel_ftr_zdb_id"),
    FEATURE_FEATURE_PREFIX(Table.FEATURE_PREFIX, Table.FEATURE, "feature_lab_prefix_id"),
    FEATURE_FEATURE_TYPE(Table.FEATURE_TYPE, Table.FEATURE, "feature_type"),
    FEATURE_ABBREV_MARKER_ABBREV(Table.MARKER, Table.FEATURE, "feature_mrkr_abbrev", "mrkr_abbrev"),
    MARKER_RELATION_1(Table.MARKER, Table.MARKER_RELATION, "mrel_mrkr_1_zdb_id"),
    MARKER_RELATION_2(Table.MARKER, Table.MARKER_RELATION, "mrel_mrkr_2_zdb_id"),
    FEATURE_RELATION_GENE(Table.MARKER, Table.FEATURE_RELATION, "fmrel_mrkr_zdb_id"),
    XPATEX_GENE(Table.MARKER, Table.EXPRESSION_EXPERIMENT, "xpatex_gene_zdb_id"),
    XPATEX_ANTIBODY(Table.MARKER, Table.EXPRESSION_EXPERIMENT, "xpatex_atb_zdb_id"),
    XPATEX_PROBE(Table.MARKER, Table.EXPRESSION_EXPERIMENT, "xpatex_probe_feature_zdb_id"),
    CLONE_GENE(Table.MARKER, Table.CLONE, "clone_mrkr_zdb_id"),
    ANTIBODY_GENE(Table.MARKER, Table.ANTIBODY, "atb_zdb_id"),
    ORTHOLOGY_GENE(Table.MARKER, Table.ORTHOLOGUE, "c_gene_id"),
    GO_EVIDENCE_GENE(Table.MARKER, Table.MARKER_GO_EVIDENCE, "mrkrgoev_mrkr_zdb_id"),
    MODIFIED_GO_EVIDENCE(Table.PERSON, Table.MARKER_GO_EVIDENCE, "mrkrgoev_modified_by"),
    EVIDENCE_CODE_GO_EVIDENCE(Table.GO_EVIDENCE_CODE, Table.MARKER_GO_EVIDENCE, "mrkrgoev_evidence_code"),
    ANNOTATION_ANNOTATION_GO_EVIDENCE(Table.MARKER_GO_TERM_EVIDENCE_ANNOTATION_ORGANIZATION, Table.MARKER_GO_EVIDENCE, "mrkrgoev_annotation_organization"),
    GO_FLAG_GO_EVIDENCE(Table.GO_FLAG, Table.MARKER_GO_EVIDENCE, "mrkrgoev_gflag_name"),
    GO_EVIDENCE_INFERENCE(Table.MARKER_GO_EVIDENCE, Table.INFERENCE_GROUP_MEMBER, "infgrmem_mrkrgoev_zdb_id"),
    DATA_NOTE_GENE(Table.MARKER, Table.DATA_NOTE, "dnote_data_zdb_id"),
    PERSON_LINKAGE(Table.PERSON, Table.LINKAGE, "lnkg_submitter_zdb_id"),
    LINKAGE_GROUP_LINKAGE(Table.LINKAGE_GROUP, Table.LINKAGE, "lnkg_or_lg"),
    LINKAGE_LINKAGE_PAIR(Table.LINKAGE, Table.LINKAGE_PAIR, "lnkgpair_linkage_zdb_id"),
    GO_EVIDENCE_PUB(Table.PUBLICATION, Table.MARKER_GO_EVIDENCE, "mrkrgoev_source_zdb_id"),
    GO_EVIDENCE_TERM(Table.TERM, Table.MARKER_GO_EVIDENCE, "mrkrgoev_term_zdb_id"),
    GO_EVIDENCE_PERSON(Table.PERSON, Table.MARKER_GO_EVIDENCE, "mrkrgoev_contributed_by"),
    DNOTE_PERSON(Table.PERSON, Table.DATA_NOTE, "dnote_curator_zdb_id"),
    MARKER_PERSON(Table.PERSON, Table.MARKER, "mrkr_owner"),
    ALIAS_MARKER_HISTORY(Table.DATA_ALIAS, Table.MARKER_HISTORY, "mhist_dalias_zdb_id"),
    MARKER_MARKER_HISTORY(Table.MARKER, Table.MARKER_HISTORY, "mhist_mrkr_zdb_id"),
    REASON_MARKER_HISTORY(Table.MARKER_HISTORY_REASON, Table.MARKER_HISTORY, "mhist_reason"),
    EVENT_MARKER_HISTORY(Table.MARKER_HISTORY_EVENT, Table.MARKER_HISTORY, "mhist_event"),
    XPATEX_PUB(Table.PUBLICATION, Table.EXPRESSION_EXPERIMENT, "xpatex_source_zdb_id"),
    NOTE_PUB(Table.PUBLICATION, Table.PUBLICATION_NOTE, "pnote_pub_zdb_id"),
    NOTE_EXT_GENO(Table.GENOTYPE, Table.EXTERNAL_NOTE, "extnote_data_zdb_id"),
    NOTE_EXT_GENOX(Table.GENOTYPE_EXPERIMENT, Table.EXTERNAL_NOTE, "extnote_data_zdb_id"),
    NOTE_EXT_MARKER(Table.MARKER, Table.EXTERNAL_NOTE, "extnote_data_zdb_id"),
    MARKER_TYPE_MARKER(Table.MARKER_TYPE, Table.MARKER, "mrkr_type"),
    MARKER_TYPE_GROUP_MARKER(Table.MARKER_TYPE, Table.MARKER_TYPE_GROUP, "mtgrp_name"),
    MARKER_MARKER_SEQUENCE(Table.MARKER, Table.MARKER_SEQUENCE, "mrkrseq_mrkr_zdb_id"),
    TYPE_MARKER_SEQUENCE(Table.SEQUENCE_TYPE, Table.MARKER_SEQUENCE, "mrkrseq_seq_type"),
    AMBIGUITY_MARKER_SEQUENCE(Table.SEQUENCE_AMBIGUITY_CODE, Table.MARKER_SEQUENCE, "mrkrseq_ambiguity_code"),
    PUBNOTE_PERSON(Table.PERSON, Table.PUBLICATION_NOTE, "pnote_curator_zdb_id"),
    CURATION_PERSON(Table.PERSON, Table.CURATION, "cur_curator_zdb_id"),
    CURATION_PUBLICATION(Table.PUBLICATION, Table.CURATION, "cur_pub_zdb_id"),
    CURATION_TOPIC_CURATION(Table.CURATION_TOPIC, Table.CURATION, "cur_topic"),
    RECORD_ATTR_PUB(Table.PUBLICATION, Table.RECORD_ATTRIBUTION, "recattrib_source_zdb_id"),
    RECORD_ATTR_MARKER(Table.MARKER, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_GENOX(Table.GENOTYPE_EXPERIMENT, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_XPATRES(Table.EXPRESSION_RESULT, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_XPAT(Table.EXPRESSION_EXPERIMENT, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_FEATURE(Table.FEATURE, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_EXT_NOTE(Table.EXTERNAL_NOTE, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_EXP(Table.EXPERIMENT, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_EXP_COND(Table.EXPERIMENT_CONDITION, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_FIG(Table.FIGURE, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_GENE(Table.MARKER, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_MREL(Table.MARKER_RELATION, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    RECORD_ATTR_GOEV(Table.MARKER_GO_EVIDENCE, Table.RECORD_ATTRIBUTION, "recattrib_data_zdb_id"),
    XPATEX_DBLINK(Table.DBLINK, Table.EXPRESSION_EXPERIMENT, "xpatex_dblink_zdb_id"),
    DBLINK_FDBCONTAINS(Table.FDBCONT, Table.DBLINK, "dblink_fdbcont_zdb_id"),
    FDBCONTAINS_TYPE(Table.FDBDT, Table.FDBCONT, "fdbcont_fdbdt_id"),
    FDBCONTAINS_DB(Table.FOREIGN_DB, Table.FDBCONT, "fdbcont_fdb_db_id"),
    BLAST_DB_FDBCONTAINS(Table.BLAST_DB, Table.FDBCONT, "fdbcont_primary_blastdb_zdb_id"),
    ORAGNISM_FDBCONTAINS(Table.ORGANISM, Table.FDBCONT, "fdbcont_organism_common_name"),
    ACCESSION_FDBCONTAINS(Table.FDBCONT, Table.ACCESSION_BANK, "accbk_fdbcont_zdb_id"),
    DBLINK_GENE(Table.MARKER, Table.DBLINK, "dblink_linked_recid"),
    MARKER_FEATSTAT(Table.MARKER, Table.FEATURE_STAT, "fstat_feat_zdb_id"),
    SUPERTERM_FEATSTAT(Table.TERM, Table.FEATURE_STAT, "fstat_superterm_zdb_id"),
    SUBTERM_FEATSTAT(Table.TERM, Table.FEATURE_STAT, "fstat_subterm_zdb_id"),
    GENE_FEATSTAT(Table.MARKER, Table.FEATURE_STAT, "fstat_gene_zdb_id"),
    PUB_FEATSTAT(Table.PUBLICATION, Table.FEATURE_STAT, "fstat_pub_zdb_id"),
    XPATRES_FEATSTAT(Table.EXPRESSION_RESULT, Table.FEATURE_STAT, "fstat_xpatres_zdb_id"),
    FIGURE_FEATSTAT(Table.FIGURE, Table.FEATURE_STAT, "fstat_fig_zdb_id"),
    IMAGE_FEATSTAT(Table.IMAGE, Table.FEATURE_STAT, "fstat_img_zdb_id"),
    GENOFEAT_GENO(Table.GENOTYPE, Table.GENOTYPE_FEATURE, "genofeat_geno_zdb_id"),
    GENOFEAT_FEATURE(Table.FEATURE, Table.GENOTYPE_FEATURE, "genofeat_feature_zdb_id"),
    GENOFEAT_DAD_ZYGO(Table.ZYGOSITY, Table.GENOTYPE_FEATURE, "genofeat_dad_zygocity"),
    GENOFEAT_MOM_ZYGO(Table.ZYGOSITY, Table.GENOTYPE_FEATURE, "genofeat_mom_zygocity"),
    GENOFEAT_ZYGO(Table.ZYGOSITY, Table.GENOTYPE_FEATURE, "genofeat_zygocity"),
    FEATURE_FEATURE_TRACKING(Table.FEATURE, Table.FEATURE_TRACKING, "ft_feature_zdb_id"),
    MO_EXP_COND(Table.MORPHOLINO, Table.EXPERIMENT_CONDITION, "expcond_mrkr_zdb_id"),
    UNIT_EXP_COND(Table.EXPERIMENT_UNIT, Table.EXPERIMENT_CONDITION, "expcond_expunit_zdb_id"),
    IMAGE_FIGURE(Table.FIGURE, Table.IMAGE, "img_fig_zdb_id"),
    DATA_ALIAS_TERM(Table.TERM, Table.DATA_ALIAS, "term:dalias_data_zdb_id"),
    TERM_ONTOLOGY(Table.ONTOLOGY, Table.TERM, "term_ontology_id"),
    IMAGE_PREPARATION(Table.IMAGE_PREPARATION, Table.IMAGE, "img_preparation"),
    IMAGE_VIEW(Table.IMAGE_VIEW, Table.IMAGE, "img_view"),
    IMAGE_DIRECTION(Table.IMAGE_DIRECTION, Table.IMAGE, "img_direction"),
    IMAGE_FORM(Table.IMAGE_FORM, Table.IMAGE, "img_form"),
    ONTOLOGY_ONT_SUBSET(Table.ONTOLOGY, Table.ONTOLOGY_SUBSET, "osubset_ont_id"),
    ONTOLOGY_SUBSET_TERM(Table.ONTOLOGY_SUBSET, Table.TERM, "term_primary_subset_id"),
    MARKER_ALIAS(Table.MARKER, Table.DATA_ALIAS, "marker:dalias_data_zdb_id"),
    FEATURE_ALIAS(Table.FEATURE, Table.DATA_ALIAS, "feature:dalias_data_zdb_id"),
    AMN_GENOTYPE(Table.GENOTYPE, Table.ALL_MAP_NAMES, "allmapnm_zdb_id"),
    ANE_AMN(Table.ALL_MAP_NAMES, Table.ALL_NAME_ENDS, "allnmend_allmapnm_serial_id"),
    DATA_ALIAS_GROUP(Table.ALIAS_GROUP, Table.DATA_ALIAS, "dalias_group_id"),
    DATA_ALIAS_SCOPE(Table.ALIAS_SCOPE, Table.DATA_ALIAS, "dalias_scope_id"),
    FEATURE_REL_TYPE(Table.FEATURE_RELATION_TYPE, Table.FEATURE_RELATION, "fmrel_type"),
    MARKER_REL_TYPE(Table.MARKER_RELATION_TYPE, Table.MARKER_RELATION, "mrel_type"),
    AMN_MARKER(Table.MARKER, Table.ALL_MAP_NAMES, "allmapnm_zdb_id"),
    EXP_PUBLICATION(Table.PUBLICATION, Table.EXPERIMENT, "exp_source_zdb_id"),
    FISH_GENE_FEATURE(Table.WH_FISH, Table.WH_GENE_FEATURE_RESULT_VIEW, "gfrv_fas_id"),
    PUBLICATION_RUN_NOMEN(Table.PUBLICATION, Table.RUN, "run_nomen_pub_zdb_id"),
    PUBLICATION_RUN_RELATION(Table.PUBLICATION, Table.RUN, "run_relation_pub_zdb_id"),
    PROGRAM_RUN(Table.RUN_PROGRAM, Table.RUN, "run_program"),
    RUN_RUN_CANDIDATE(Table.RUN, Table.RUN_CANDIDATE, "runcan_run_zdb_id"),
    CANDIDATE_RUN_CANDIDATE(Table.CANDIDATE, Table.RUN_CANDIDATE, "runcan_cnd_zdb_id"),
    PERSON_RUN_CANDIDATE(Table.PERSON, Table.RUN_CANDIDATE, "runcan_locked_by"),

    TERM_SUBSET_ASSOC(Table.TERM, Table.ONTOLOGY_SUBSET, Table.TERM_SUBSET),
    SUBSET_TERM_ASSOC(Table.ONTOLOGY_SUBSET, Table.TERM, Table.SUBSET_TERM),

    WH_FISH_GENOTYPE(Table.GENOTYPE, Table.WH_FISH, "fas_genotype_group"),
    WH_FEATURE_RESULT_GENE(Table.MARKER, Table.WH_GENE_FEATURE_RESULT_VIEW, "gfrv_gene_zdb_id"),
    WH_FEATURE_RESULT_FEATURE(Table.FEATURE, Table.WH_GENE_FEATURE_RESULT_VIEW, "gfrv_affector_id"),
    WH_FEATURE_RESULT_MORPHOLINO(Table.MARKER, Table.WH_GENE_FEATURE_RESULT_VIEW, "gfrv_affector_id"),
    WH_FEATURE_RESULT_CONSTRUCT(Table.MARKER, Table.WH_GENE_FEATURE_RESULT_VIEW, "gfrv_construct_zdb_id"),
    //WH_FAS_PFIGG(Table.WH_FISH, Table.WH_PHENOTYPE_FIGURE_GROUP, "pfigg_group_name", "fas_pheno_figure_group"),
    WH_FTFS_FAS(Table.WH_FISH, Table.WH_FIGURE_TERM_FISH_SEARCH, "ftfs_fas_id"),
    //WH_FAS_MORPHGG(Table.WH_FISH, Table.WH_MORPHOLINO_GROUP, "morphg_group_name", "fas_morpholino_group"),
    //WH_FTFS_TERMGROUP(Table.WH_TERM_GROUP, Table.WH_FIGURE_TERM_FISH_SEARCH, "ftfs_term_group"),
    WH_FAS_PFIGM(Table.WH_PHENOTYPE_FIGURE_GROUP, Table.WH_PHENOTYPE_FIGURE_GROUP_MEMBER, "pfiggm_group_id"),
    WH_FAS_MORPHGM(Table.WH_MORPHOLINO_GROUP, Table.WH_MORPHOLINO_GROUP_MEMBER, "morphgm_group_id"),
    WH_FTFS_FIGURE(Table.FIGURE, Table.WH_FIGURE_TERM_FISH_SEARCH, "ftfs_fig_zdb_id");

    private Table entityTable;
    private Table foreignKeyTable;
    private Table manyToManyTable;
    private boolean manyToManyRelationship;
    private String foreignKey;
    // this is used in case the foreign key is not on the PK of the
    // foreign key table but a different column
    private String nonPkLookupKey;

    ForeignKey(Table entityTable, Table foreignKeyTable, Table manyToManyTable) {
        this.entityTable = entityTable;
        this.foreignKeyTable = foreignKeyTable;
        this.manyToManyTable = manyToManyTable;
        manyToManyRelationship = true;
    }

    ForeignKey(Table entityTable, Table foreignKeyTable, String foreignKey, String matchingPrimaryKey) {
        this.entityTable = entityTable;
        this.foreignKeyTable = foreignKeyTable;
        this.foreignKey = foreignKey;
        this.nonPkLookupKey = matchingPrimaryKey;
    }

    ForeignKey(Table entityTable, Table foreignKeyTable, String foreignKey) {
        this.entityTable = entityTable;
        this.foreignKeyTable = foreignKeyTable;
        this.foreignKey = foreignKey;
    }

    public Table getEntityTable() {
        return entityTable;
    }

    public Table getForeignKeyTable() {
        return foreignKeyTable;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public String getNonPkLookupKey() {
        return nonPkLookupKey;
    }

    public static ForeignKey getForeignKey(Table table, Table foreignKeyTable) {
        if (table == null || foreignKeyTable == null)
            return null;
        for (ForeignKey foreignKey : values())
            if (foreignKey.getEntityTable().equals(table) && foreignKey.getForeignKeyTable().equals(foreignKeyTable))
                return foreignKey;
        return null;
    }

    public static ForeignKey getForeignKeyByColumnName(String columnName) {
        if (columnName == null)
            return null;
        for (ForeignKey foreignKey : values()) {
            if (!foreignKey.isManyToManyRelationship() && foreignKey.getForeignKey().equals(columnName))
                return foreignKey;
            if (foreignKey.isManyToManyRelationship() && foreignKey.getManyToManyTable().getPkName().equals(columnName))
                return foreignKey;
        }
        return null;
    }

    public static List<ForeignKey> getForeignKeys(Table table) {
        if (table == null)
            return null;
        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>();
        for (ForeignKey foreignKey : values())
            if (foreignKey.getEntityTable().equals(table))
                foreignKeyList.add(foreignKey);
        return foreignKeyList;
    }

    public static List<ForeignKey> getForeignTrueKeys(Table table) {
        if (table == null)
            return null;
        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>();
        for (ForeignKey foreignKey : values())
            if (foreignKey.getForeignKeyTable().equals(table))
                foreignKeyList.add(foreignKey);
        return foreignKeyList;
    }

    /**
     * retrieves all tables that connect a given root table with a foreign key column.
     *
     * @param columnName foreign key column name
     * @param rootTable  table
     * @return list of FKs
     */
    public static List<ForeignKey> getJoinedForeignKeys(String columnName, String rootTable) {
        List<ForeignKey> joinTables = new ArrayList<ForeignKey>(3);
        ForeignKey foreignKey = getForeignKeyByColumnName(columnName);
        joinTables.add(foreignKey);
        Table firstJoinTable = foreignKey.getForeignKeyTable();
        if (firstJoinTable.getTableName().equalsIgnoreCase(rootTable))
            return joinTables;

        // check all foreign keys for further FK relationships until one
        // has a relationship with the root table.
        if (!foundAssociationPath(joinTables, firstJoinTable, rootTable))
            return null;
        return joinTables;
    }

    private static boolean foundAssociationPath(List<ForeignKey> joinTables, Table table, String rootTableName) {
        List<ForeignKey> foreignKeys = getForeignKeysByJoinTable(table);
        boolean foundPath = false;
        for (ForeignKey foreignKeyNext : foreignKeys) {
            // found the matching association. Return the result path
            if (foreignKeyNext.getForeignKeyTable().getTableName().equalsIgnoreCase(rootTableName)) {
                joinTables.add(foreignKeyNext);
                return true;
            }
            // this path was wrong
            if (getForeignKeysByJoinTable(foreignKeyNext.getForeignKeyTable()).size() == 0)
                continue;
            joinTables.add(foreignKeyNext);
            foundPath = foundAssociationPath(joinTables, foreignKeyNext.getForeignKeyTable(), rootTableName);
            if (foundPath)
                return true;
            joinTables.remove(joinTables.size() - 1);
        }
        return false;
    }

    public static List<ForeignKey> getForeignKeysByJoinTable(Table joinTable) {
        List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>(3);
        for (ForeignKey joinedTableFK : values()) {
            Table nextJoinTable = joinedTableFK.getEntityTable();
            if (nextJoinTable == joinTable)
                foreignKeys.add(joinedTableFK);
        }
        return foreignKeys;
    }

    public static List<ForeignKey> getForeignKeysByColumnName(String columnName) {
        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>(2);
        if (columnName == null)
            return null;
        for (ForeignKey foreignKey : values()) {
            if (!foreignKey.isManyToManyRelationship() && foreignKey.getForeignKey().equals(columnName))
                foreignKeyList.add(foreignKey);
        }
        return foreignKeyList;
    }

    /**
     * Retrieve
     *
     * @param table
     * @return
     */
    public static List<ForeignKey> getManyToManyForeignKeys(Table table) {
        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>(2);
        if (table == null)
            return null;
        for (ForeignKey foreignKey : values()) {
            if (foreignKey.isManyToManyRelationship())
                if (foreignKey.getForeignKeyTable().equals(table))
                    foreignKeyList.add(foreignKey);
        }
        return foreignKeyList;
    }

    public static List<Table> getReferencedBy(Table table) {
        List<Table> referenceTables = new ArrayList<Table>(5);
        for (ForeignKey foreignKey : values())
            if (foreignKey.getEntityTable().equals(table))
                referenceTables.add(foreignKey.getForeignKeyTable());
        return referenceTables;

    }

    protected static Map<Table, Map<Integer, List<ForeignKey>>> dagMap = null;

    /**
     * Retrieve all Foreign Key Tables for a given table and level.
     *
     * @param table      table
     * @param levelDepth level
     * @return list of foreign keys
     */
    public static List<ForeignKey> getForeignKeys(Table table, int levelDepth) {
        if (levelDepth < 1)
            return null;

        if (dagMap == null) {
            dagMap = new HashMap<Table, Map<Integer, List<ForeignKey>>>(Table.values().length);
            createDagMap();

        }
        Map<Integer, List<ForeignKey>> tableDag = dagMap.get(table);
        if (tableDag == null)
            return null;
        return tableDag.get(levelDepth);
    }

    private static void createDagMap() {
        for (Table table : Table.values()) {
            createDagMap(table);
        }
    }

    protected static void createDagMap(Table table) {
        boolean foundKeys = true;
        Map<Integer, List<ForeignKey>> map = new HashMap<Integer, List<ForeignKey>>();
        int level = 1;
        List<ForeignKey> levelFK = getForeignKeys(table);
        map.put(level, getForeignKeys(table));
        while (foundKeys && level < 10) {
            level++;
            List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>();
            for (ForeignKey foreignKey : levelFK) {
                List<ForeignKey> foreignKeys = getForeignKeys(foreignKey.getForeignKeyTable());
                if (foreignKeys != null && foreignKeys.size() > 0) {
                    // ensure that we do not end up with an infinite loop and
                    // keep adding the same FKs.
                    if (!foreignKeyList.contains(foreignKeys.get(0)))
                        foreignKeyList.addAll(foreignKeys);
                }
            }
            if (foreignKeyList.size() > 0) {
                map.put(level, foreignKeyList);
                levelFK = foreignKeyList;
            } else
                foundKeys = false;
        }
        dagMap.put(table, map);
    }

    public static boolean hasForeignKeys(Table table) {
        return getForeignKeys(table) != null && getForeignKeys(table).size() > 0;
    }

    public boolean isPKLookup() {
        return nonPkLookupKey == null;
    }

    public boolean isManyToManyRelationship() {
        return manyToManyRelationship;
    }

    public Table getManyToManyTable() {
        return manyToManyTable;
    }

    /**
     * Retrieve list of FKs by FK names comma-delimited
     *
     * @param foreignKeyName fk name
     * @return list of foreign keys
     */
    public static List<ForeignKey> getForeignKeyHierarchy(String foreignKeyName, Table rootTable) {
        return null;
    }

    /**
     * Retrieve list of FKs by FK names comma-delimited
     *
     * @param foreignKeyName fk name
     * @return list of foreign keys
     */
    public static List<ForeignKey> getForeignKeyHierarchy(String foreignKeyName) {
        if (foreignKeyName == null)
            return null;
        String[] foreignKeyNames = foreignKeyName.split(DELIMITER);
        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>(foreignKeyNames.length);
        for (String foreignKey : foreignKeyNames)
            foreignKeyList.add(getForeignKeyByColumnName(foreignKey));
        return foreignKeyList;
    }

    /**
     * Retrieve list of FKs by FK names comma-delimited
     *
     * @param foreignKeyName
     * @return
     */
    public static String getForeignKeyHierarchyName(String foreignKeyName) {
        if (foreignKeyName == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (ForeignKey foreignKey : getForeignKeyHierarchy(foreignKeyName)) {
            builder.append(foreignKey.getForeignKey());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * Assumes the first fk name is points to the root table
     *
     * @param fullNodeName
     * @return
     */
    public static Table getRootTableFromNodeName(String fullNodeName) {
        if (fullNodeName == null)
            return null;
        return getForeignKeyByColumnName(fullNodeName.split(DELIMITER)[0]).getEntityTable();
    }

    public static final String DELIMITER = "\\|";
}
