package org.zfin.database.presentation;

import org.zfin.database.DatabaseService;
import org.zfin.marker.Marker;

import java.util.*;

public enum Table {

    ACCESSION_BANK("ACCESSION", "accession_bank", "accbk_pk_id"),
    ALIAS_GROUP("ALIASGROUP", "alias_group", "aliasgrp_pk_id", "aliasgrp_name"),
    ALIAS_SCOPE("ALIASSCOPE", "alias_scope", "aliasscope_pk_id", "aliasscope_scope"),
    ALL_MAP_NAMES("AMN", "all_map_names", "allmapnm_serial_id"),
    ALL_NAME_ENDS("ANE", "all_name_ends", "allnmend_name_end_lower,allnmend_allmapnm_serial_id"),
    ALL_TERM_CONTAINS("ALLTERM", "all_term_contains", null, ""),
    ANATOMY_DISPLAY("MARKER", "anatomy_display", "anatdisp_stg_zdb_id,anatdisp_seq_num"),
    ANATOMY_ITEM("ANAT", "anatomy_item", "anatitem_zdb_id","anatitem_name","anatitem_is_cell,anatitem_is_obsolete"),
    ANATOMY_STATS("ANATOMY_STATS", "anatomy_stats", "anatstat_term_zdb_id,anatstat_object_type"),
    ANTIBODY("MARKER", "antibody", "atb_zdb_id"),
    APATO_TAG("PATOTAG", "apato_tag", "apatotag_name"),
    CLONE("MARKER", "clone", "clone_mrkr_zdb_id"),
    BLAST_DB("BLASTDB", "blast_database", "blastdb_zdb_id","blastdb_name"),
    BLAST_DB_ORINGINATION_TYPE("BLAST_DB_ORINGINATION_TYPE", "blast_database_origination_type", "bdot_pk_id"),
    BLAST_DB_TYPE("BLAST_DB_TYPE", "blast_database_type", "bdbt_type","blastdb_public"),
    BLAST_HIT("BHIT", "blast_hit", "bhit_zdb_id"),
    BLAST_QUERY("BQRY", "blast_query", "bqry_zdb_id"),
    BLAST_REPORT("BRPT", "blast_report", "cnd_zdb_id"),
    BLASTDB_ORDER("BLASTDB_ORDER", "blastdb_order", "bdborder_pk_id"),
    BLASTDB_REGEN_CONTENT("BLASTDB_REGEN_CONTENT", "blastdb_regen_content", "brc_pk_id"),
    //BLASTDB_RUN("BLASTDB_REGEN_CONTENT", "blastdb_run", "brc_pk_id"),
    CANDIDATE("CND", "candidate", "cnd_zdb_id", "cnd_suggested_name"),
    CONDITION_DATA_TYPE("CDT", "condition_data_type", "cdt_zdb_id"),
    CURATION("CUR", "curation", "cur_zdb_id"),
    CURATION_TOPIC("CURTOPIC", "curation_topic", "curtopic_name"),
    COMPANY("COMPANY", "company", "zdb_ID", "name"),
    DATA_ALIAS("DALIAS", "data_alias", "dalias_zdb_id","dalias_alias"),
    DATA_NOTE("DNOTE", "data_note", "dnote_zdb_id"),
    DBLINK("DBLINK", "db_link", "dblink_zdb_id", "dblink_acc_num_display"),
    EXPERIMENT("EXP", "experiment", "exp_zdb_id", "exp_name"),
    EXPERIMENT_CONDITION("EXPCOND", "experiment_condition", "expcond_zdb_id", "expcond_comments"),
    EXPERIMENT_UNIT("EXPUNIT", "experiment_unit", "expunit_zdb_id", "expunit_name"),
    EXPRESSION_EXPERIMENT("XPAT", "expression_experiment", "xpatex_zdb_id", null, "xpatex_assay_name"),
    EXPRESSION_RESULT("XPATRES", "expression_result", "xpatres_zdb_id", null, "xpatres_expression_found"),
    EXTERNAL_NOTE("EXTNOTE", "external_note", "extnote_zdb_id"),
    FOREIGN_DB("FOREIGNDB", "foreign_db", "fdb_db_pk_id", "fdb_db_name"),
    FDBCONT("FDBCONT", "foreign_db_contains", "fdbcont_zdb_id", "fdbcont_organism_common_name"),
    FDBDT("FDBDT", "foreign_db_data_type", "fdbdt_pk_id", "fdbdt_data_type"),
    FEATURE("ALT", "feature", "feature_zdb_id", "feature_abbrev", "feature_type,feature_lab_prefix_id,feature_mrkr_abbrev,feature_dominant,feature_unspecified,feature_unrecovered,feature_known_insertion_site"),
    FEATURE_ASSAY("FEATASSAY", "feature_assay", "featassay_pk_id", "featassay_mutagen", null),
    FEATURE_HISTORY("FHIST", "feature_history", "fhist_zdb_id"),
    FEATURE_STAT("FEATSTAT", "feature_stats", "fstat_pk_id", null, "fstat_type"),
    FEATURE_PREFIX("FEATPRE", "feature_prefix", "fp_pk_id", "fp_institute_display"),
    FEATURE_RELATION("FMREL", "feature_marker_relationship", "fmrel_zdb_id", "fmrel_type", "fmrel_type", "fmrel_ftr_zdb_id,fmrel_mrkr_zdb_id"),
    FEATURE_RELATION_TYPE("FMRELTYPE", "feature_marker_relationship_type", "fmreltype_name"),
    FEATURE_TRACKING("FEATTRACK", "feature_tracking", "ft_pk_id", "ft_feature_abbrev"),
    FEATURE_TYPE("ALTTYPE", "feature_type", "ftrtype_name"),
    FEATURE_TYPE_GROUP("FEATURE_TYPE_GROUP", "FEATURE_TYPE_GROUP", "ftrgrp_name"),
    FIGURE("FIG", "figure", "fig_zdb_id", "fig_label"),
    //FISH_SEARCH("FISH_SEARCH", "fish_search", "fish_id", "name"),
    GENOTYPE("GENO", "genotype", "geno_zdb_id", "geno_display_name"),
    GENOTYPE_EXPERIMENT("GENOX", "genotype_experiment", "genox_zdb_id", "genox_geno_zdb_id,genox_exp_zdb_id", "genox_is_standard,genox_is_std_or_generic_control"),
    GENOTYPE_FEATURE("GENOFEAT", "genotype_feature", "genofeat_zdb_id"),
    GO_EVIDENCE_CODE("GOEVCODE", "go_evidence_code", "goev_code"),
    GO_FLAG("GO_FLAG", "go_flag", "gflag_name"),
    IMAGE("IMAGE", "image", "img_zdb_id", "img_label", "img_preparation,img_view,img_direction,img_form"),
    IMAGE_PREPARATION("IMAGEPREP", "image_preparation", "imgprep_name", "imgprep_name"),
    IMAGE_VIEW("IMAGEVIEW", "image_view", "imgview_name", "imgview_name"),
    IMAGE_FORM("IMAGEFORM", "image_form", "imgform_name", "imgform_name"),
    IMAGE_DIRECTION("IMAGEFIR", "image_direction", "imgdir_name", "imgdir_name"),
    INFERENCE_GROUP_MEMBER("INFERRED", "inference_group_member", "infgrmem_inferred_from"),
    INT_DATA_SUPPLIER("INT_DATA_SUPPLIER", "int_data_supplier", "idsup_data_zdb_id,idsup_supplier_zdb_id"),
    INT_IMAGE_TERM("INT_IMAGE_TERM", "int_image_term", "iit_term_zdb_id,iit_img_zdb_id"),
    INT_PERSON_LAB("INT_PERSON_LAB", "int_person_lab", "source_id,target_id"),
    INT_PERSON_PUB("INT_PERSON_PUB", "int_person_pub", "source_id,target_id"),
    INT_PERSON_COMPANY("INT_PERSON_COPMPANY", "int_person_company", "source_id,target_id"),
    JOURNAL("JRNL", "journal", "jrnl_zdb_id"),
    LAB("LAB", "lab", "zdb_id", "name"),
    LINKAGE("LINK", "linkage", "lnkg_zdb_id", "lnkg_or_lg"),
    LINKAGE_GROUP("LINK_GROUP", "linkage_group", "lg_name"),
    LINKAGE_PAIR("LINK_PAIR", "linkage_pair", "lnkgpair_zdb_id"),
    MARKER("MARKER", "marker", "mrkr_zdb_id", "mrkr_abbrev"),
    MARKER_HISTORY("NOMEN", "marker_history", "mhist_zdb_id"),
    MARKER_HISTORY_EVENT("MARKER_HISTORY_EVENT", "marker_history_event", "mhistvnt_name"),
    MARKER_HISTORY_REASON("MARKER_HISTORY_REASON", "marker_history_reason", "mhistrsn_name"),
    MARKER_GO_EVIDENCE("MRKRGOEV", "marker_go_term_evidence", "mrkrgoev_zdb_id"),
    MARKER_GO_TERM_EVIDENCE_ANNOTATION_ORGANIZATION("GO_ANNOT_ORG", "marker_go_term_evidence_annotation_organization", "mrkgoevas_pk_id", "mrkrgoevas_annotation_organization"),
    MARKER_RELATION("MREL", "marker_relationship", "mrel_zdb_id", "mrel_type ", "mrel_type", "mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id"),
    MARKER_RELATION_TYPE("MRELTYPE", "marker_relationship_type", "mreltype_name"),
    MARKER_SEQUENCE("MRKRSEQ", "marker_sequence", "mrkrseq_zdb_id"),
    MARKER_TYPE("MARKERTYPE", "marker_types", "marker_type"),
    MARKER_TYPE_GROUP("MARKERTYPEGRP", "marker_type_group", "mtgrp_name"),
    MARKER_TYPE_GROUP_MEMBER("MARKERTYPEGRPMEM", "marker_type_group_member", "mtgrpmem_mkrk_type"),
    MORPHOLINO("MRPHLNO", "marker", "mrkr_zdb_id", "mrkr_abbrev"),
    MUTAGEN("MUTAGEN", "mutagen", "mutagen_name"),
    MUTAGEE("MUTAGEE", "mutagee", "mutagee_name"),
    OBSOLETE_TERM_REPLACEMENT("OBSOLETE_TERM_REPLACEMENT", "obsolete_term_replacement", "obstermrep_pk_id"),
    OBSOLETE_TERM_SUGGESTION("OBSOLETE_TERM_SUGGESTION", "obsolete_term_suggestion", "obstermsug_pk_id"),
    ONTOLOGY("ONTOLOGY", "ontology", "ont_pk_id", "ont_ontology_name"),
    ONTOLOGY_SUBSET("ONT_SUBSET", "ontology_subset", "osubset_pk_id", "osubset_subset_name"),
    ORGANISM("ORGANISM", "organism", "organism_common_name"),
    ORTHOLOGUE("ORTHO", "orthologue", "zdb_id"),
    ORTHOLOGUE_EVIDENCE("ORTHOEV", "orthologue_evidence", "oev_ortho_zdb_id"),
    PERSON("PERS", "person", "zdb_id", "name"),
    PHENOTYPE_EXPERIMENT("PHENOX", "phenotype_experiment", "phenox_pk_id"),
    PHENOTYPE_STATEMENT("PHENOS", "phenotype_statement", "phenos_pk_id", null, "phenos_tag"),
    PUBLICATION("PUB", "publication", "zdb_id", "pub_mini_ref "),
    PUBLICATION_NOTE("PNOTE", "publication_note", "pnote_zdb_id"),
    RECORD_ATTRIBUTION("REC_ATTR", "record_attribution", "recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type"),
    RUN("RUN", "run", "run_zdb_id","run_name"),
    RUN_CANDIDATE("RUNCAN", "run_candidate", "runcan_zdb_id"),
    RUN_PROGRAM("RUN_PROGRAM", "run_program", "runprog_program"),
    RUN_TYPE("RUN_TYPE", "run_type", "runtype_name"),
    SEQUENCE_TYPE("SEQUENCE_TYPE", "sequence_type", "seqtype_name"),
    SEQUENCE_AMBIGUITY_CODE("SEQUENCE_AMBIGUITY_CODE", "sequence_ambiguity_code", "seqac_symbol", "seqac_meaning"),
    STAGE("STAGE", "stage", "stg_zdb_id", "stg_abbrev"),
    TERM("TERM", "term", "term_zdb_id", "term_name", "term_is_obsolete,term_is_secondary,term_is_root"),
    TERM_RELATIONSHIP("TERMREL", "term_relationship", "termrel_zdb_id", null),
    TERM_RELATIONSHIP_TYPE("TERMRELTYPE", "term_relationship_type", "termreltype_name"),
    TERM_STATS("TERM_STAT", "anatomy_stats", "anatstat_term_zdb_id"),
    ZYGOSITY("ZYG", "zygocity", "zyg_zdb_id", "zyg_name"),

    // many-to-many relationships
    LINK_MEMBER("LINK_MEMBER", "link_member", "lnkgmem_linkage_zdb_id,lnkgmem_member_zdb_id", LINKAGE, MARKER),
    MEMBER_LINKAGE("MEMBER_LINKAGE", "link_member", "lnkgmem_member_zdb_id,lnkgmem_linkage_zdb_id", MARKER, LINKAGE),
    TERM_SUBSET("TERM_SUBSET", "term_subset", "termsub_term_zdb_id,termsub_subset_id", TERM, ONTOLOGY_SUBSET),
    SUBSET_TERM("SUBSET_TERM", "term_subset", "termsub_subset_id,termsub_term_zdb_id", ONTOLOGY_SUBSET, TERM),
    FIGURE_XPATRES("FIGURE_EXPRESSION", "expression_pattern_figure", "xpatfig_fig_zdb_id,xpatfig_xpatres_zdb_id", FIGURE, EXPRESSION_RESULT),
    XPATRES_FIGURE("EXPRESSION_FIGURE", "expression_pattern_figure", "xpatfig_xpatres_zdb_id,xpatfig_fig_zdb_id", EXPRESSION_RESULT, FIGURE),
    PERSON_LAB("PERSON_LAB", "int_person_lab", "source_id,target_id", PERSON, LAB),
    LAB_PERSON("LAB_PERSON", "int_person_lab", "target_id,source_id", LAB, PERSON),
    PERSON_PUB("PERSON_PUB", "int_person_pub", "source_id,target_id", PERSON, PUBLICATION),
    PUB_PERSON("PUB_PERSON", "int_person_pub", "target_id,source_id", PUBLICATION, PERSON),
    PERSON_COMP("PERSON_COMP", "int_person_company", "source_id,target_id", PERSON, COMPANY),
    COMP_PERSON("COMP_PERSON", "int_person_company", "target_id,source_id", COMPANY, PERSON),
    TERM_IMAGE("TERM_IMAGE", "int_image_term", "iit_term_zdb_id,iit_img_zdb_id", TERM, IMAGE),
    IMAGE_TERM("IMAGE_TERM", "int_image_term", "iit_img_zdb_id,iit_term_zdb_id", IMAGE, TERM),
    MARKER_LAB("DATA_LAB", "int_data_supplier", "marker:idsup_data_zdb_id,lab:idsup_supplier_zdb_id", MARKER, LAB),
    MARKER_COMP("DATA_COMP", "int_data_supplier", "marker:idsup_data_zdb_id,company:idsup_supplier_zdb_id", MARKER, COMPANY),
    GENO_LAB("GENO_LAB", "int_data_supplier", "genotype:idsup_data_zdb_id,lab:idsup_supplier_zdb_id", GENOTYPE, LAB),
    GENO_COMP("GENO_COMP", "int_data_supplier", "genotype:idsup_data_zdb_id,company:idsup_supplier_zdb_id", GENOTYPE, COMPANY),
    FEATURE_LAB("FEATURE_LAB", "int_data_supplier", "feature:idsup_data_zdb_id,lab:idsup_supplier_zdb_id", FEATURE, LAB),
    FEATURE_COMP("FEATURE_COMP", "int_data_supplier", "feature:idsup_data_zdb_id,company:idsup_supplier_zdb_id", FEATURE, COMPANY),
    LAB_MARKER("LAB_MARKER", "int_data_supplier", "lab:idsup_supplier_zdb_id,marker:idsup_data_zdb_id", LAB, MARKER),
    COMP_MARKER("COMP_MARKER", "int_data_supplier", "company:idsup_supplier_zdb_id,marker:idsup_data_zdb_id", COMPANY, MARKER),
    LAB_GENO("LAB_GENO", "int_data_supplier", "lab:idsup_supplier_zdb_id,genotype:idsup_data_zdb_id", LAB, GENOTYPE),
    COMP_GENO("COMP_GENO", "int_data_supplier", "company:idsup_supplier_zdb_id,genoypte:idsup_data_zdb_id", COMPANY, GENOTYPE),
    LAB_FEATURE("LAB_FEATURE", "int_data_supplier", "lab:idsup_supplier_zdb_id,feature:idsup_data_zdb_id", LAB, FEATURE),
    COMP_FEATURE("OMP_FEATURE", "int_data_supplier", "company:idsup_supplier_zdb_id,feature:idsup_data_zdb_id", COMPANY, FEATURE),

    // warehouse tables
    WH_FISH("FISH", "fish_annotation_search", "fas_pk_id", null, "fas_affector_type_group"),
    WH_FIGURE_TERM_FISH_SEARCH("FTFS", "figure_term_fish_search", "ftfs_pk_id"),
    WH_GENE_FEATURE_RESULT_VIEW("GENE_FEATURE_RESULT_VIEW", "gene_feature_result_view", "gfrv_pk_id", null, "gfrv_affector_type_display", "gfrv_affector_id,gfrv_construct_zdb_id"),
    WH_MORPHOLINO_GROUP("WHMORPHGG", "morpholino_group", "morphg_group_pk_id"),
    WH_MORPHOLINO_GROUP_MEMBER("WHMORPHGM", "morpholino_group_member", "morphgm_pk_id"),
    WH_PHENOTYPE_FIGURE_GROUP("PFIGG", "phenotype_figure_group", "pfigg_group_pk_id"),
    WH_PHENOTYPE_FIGURE_GROUP_MEMBER("PFIGM", "phenotype_figure_group_member", "pfiggm_pk_id"),
    WH_TERM_GROUP("TG", "term_group", "tg_group_name"),;

    private String pkIdentifier;
    private String tableName;
    private String pkName;
    private List<String> primaryKeyColumns = new ArrayList<String>(1);
    private String entityNameColumn;
    private String dictionaryColumnsString;
    private String zdbDictionaryColumnsString;
    // cached value
    private List<Table> children;

    Table(String pkIdentifier, String tableName, String pkName) {
        this.pkIdentifier = pkIdentifier;
        this.tableName = tableName;
        if (pkName == null)
            return;
        String[] cols = pkName.split(",");
        Collections.addAll(primaryKeyColumns, cols);
        this.pkName = pkName;
    }

    Table(String pkIdentifier, String tableName, String pkName, String entityName) {
        this(pkIdentifier, tableName, pkName);
        this.entityNameColumn = entityName;
    }

    public static void validateUniquePkIdentifier() {
        Set<String> pkIdentifiers = new HashSet<String>();
        for (Table table : values()) {
            String pkIdentifier = table.getPkIdentifier();
            if (!pkIdentifiers.add(pkIdentifier) && !pkIdentifier.equals("MARKER"))
                throw new RuntimeException("Table " + table.getTableName() + " with PK ID " + pkIdentifier + " already exists: ");
        }
    }

    Table(String pkIdentifier, String tableName, String pkName, String entityName, String dictionaryColumns) {
        this(pkIdentifier, tableName, pkName, entityName);
        this.dictionaryColumnsString = dictionaryColumns;
    }

    Table(String pkIdentifier, String tableName, String pkName, String entityName, String dictionaryColumns, String zdbDictionaryColumnsString) {
        this(pkIdentifier, tableName, pkName, entityName, dictionaryColumns);
        this.zdbDictionaryColumnsString = zdbDictionaryColumnsString;
    }

    private Map<Table, String> manyToManyTableMap;

    Table(String pkIdentifier, String tableName, String pkName, Table tableOne, Table tableTwo) {
        this(pkIdentifier, tableName, pkName);
        manyToManyTableMap = new HashMap<Table, String>(2);
        manyToManyTableMap.put(tableOne, primaryKeyColumns.get(0));
        manyToManyTableMap.put(tableTwo, primaryKeyColumns.get(1));
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPkName() {
        return pkName;
    }

    public String getPkIdentifier() {
        return pkIdentifier;
    }

    public String getEntityNameColumn() {
        return entityNameColumn;
    }

    public String getEntityName(String id) {
        if (entityNameColumn == null)
            return null;

        return DatabaseService.getEntityName(id, this);
    }

    public boolean isEntityName() {
        return entityNameColumn != null;
    }

    public String getDictionaryColumnsString() {
        return dictionaryColumnsString;
    }

    public boolean hasDictionaryColumns() {
        return dictionaryColumnsString != null;
    }

    public List<String> getDictionaryColumns() {
        if (dictionaryColumnsString == null)
            return null;
        String[] columnArray = dictionaryColumnsString.split(",");
        return Arrays.asList(columnArray);
    }

    public String getZdbDictionaryColumnsString() {
        return zdbDictionaryColumnsString;
    }

    public boolean hasZdbDictionaryColumns() {
        return zdbDictionaryColumnsString != null;
    }

    public List<String> getZdbDictionaryColumns() {
        if (zdbDictionaryColumnsString == null)
            return null;
        String[] columnArray = zdbDictionaryColumnsString.split(",");
        return Arrays.asList(columnArray);
    }

    public List<Table> getReferencedBy() {
        return ForeignKey.getReferencedBy(this);
    }

    public List<ForeignKey> getFkReferences() {
        return ForeignKey.getForeignKeysByJoinTable(this);
    }

    public static Table getEntityTable(String zdbID) {
        if (zdbID == null)
            return null;
        String[] tokens = zdbID.split("-");
        String identifier = null;
        if (tokens.length < 2)
            return null;
        // either serial PK or a group of ZDB ids
        if (tokens.length == 2 || tokens.length > 4)
            identifier = tokens[0];
        else
            identifier = tokens[1];
        for (Table entityTable : values())
            if (entityTable.getPkIdentifier().equalsIgnoreCase(identifier))
                return entityTable;
        // check if any of the marker types
        if (Marker.Type.isMarkerType(identifier))
            return MARKER;
        // Try the first token
        identifier = tokens[0];
        for (Table entityTable : values())
            if (entityTable.getPkIdentifier().equalsIgnoreCase(identifier))
                return entityTable;
        return null;
    }

    public static Table getEntityTableByTableName(String table) {
        if (table == null)
            return null;
        for (Table entityTable : values())
            if (entityTable.getTableName().equals(table))
                return entityTable;
        return null;
    }

    public boolean isPrimaryKey(String name) {
        return primaryKeyColumns.contains(name);
    }

    public List<Table> getChildTables() {
        if (children != null)
            return children;
        List<ForeignKey> fkList = ForeignKey.getForeignKeys(this);
        if (fkList == null)
            return null;
        children = new ArrayList<Table>(fkList.size());
        for (ForeignKey fk : fkList)
            children.add(fk.getForeignKeyTable());
        return children;
    }

    public boolean hasForeignKeys() {
        return ForeignKey.getForeignTrueKeys(this) != null;
    }

    public String getForeignKey(Table rootTable) {
        if (manyToManyTableMap == null)
            throw new RuntimeException("No many-to-many Key relation defined");
        return manyToManyTableMap.get(rootTable);
    }
}
