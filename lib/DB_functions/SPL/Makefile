#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SQL/SPL procedures.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The routines defined in this directory are written in SPL, the Informix
# Stored Procedure language.  Files must have the same name as the routine 
# they define.  Each file defines only one routine (except for a few cases,
# see below).
# See the routines themselves for comments on their parameters and what 
# they do.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_functions/SPL
SUBDIRS = Regen_names Regen_genox

# Group the SPLs according to these characteristcs
# 1. General area the SPL is for.  This is basically a comment.
# 2. Generic or static.  The makefile needs to know this.

DBLINK_STATS = get_dblink_acc_num_display.sql p_dblink_has_parent.sql \
	       p_check_caps_acc_num.sql p_one_ortho_per_db.sql p_update_dblink_length.sql \
	       get_genbank_dblink_length_type.sql increment_candidate_occurrences.sql \
	       getZfinAccessionNumberNoInsert.sql \
	       getZfinAccessionNumber.sql \
	       setTscriptLoadId.sql
FISH_STATS   = get_image_stats.sql \
	       p_update_related_genotype_names.sql \
	       p_update_fmrel_genotype_names.sql \
               fhist_event.sql \
	       p_update_unspecified_alleles.sql \
	       p_feature_stats_check_constraints.sql \
	       p_fmrel_grpmem_correct.sql \
	       get_feature_abbrev_display.sql \
	       get_feature_abbrev_html.sql \
	       get_genotype_backgrounds.sql \
	       get_genotype_display.sql \
	       get_genotype_handle.sql \
	       p_update_geno_nickname.sql \
	       get_feature_type.sql \
	       p_check_pato_postcomposed_terms.sql \
	       p_check_fx_postcomposed_terms.sql \
	       p_markers_present_absent_exclusive.sql \
	       checkFeatureAbbrev.sql \
	       updateConstructAbbrev.sql \
	       checkDupFeaturePrefixLineDesignation.sql 
LOAD_STATS   = p_zgc_load.sql
XPAT_STATS   = populate_direct_submission_date.sql
MRKR_STATS   = gene_contains_seg.sql mhist_event.sql p_mrel_grpmem_correct.sql \
	       p_2_linkage_pairs.sql p_check_zdb_id_in_genedom.sql \
	       p_goterm_not_obsolete.sql p_marker_has_goterm.sql \
	       p_term_is_not_obsolete_or_secondary.sql \
	       p_check_mrkr_abbrev.sql \
               p_insert_into_record_attribution_tablezdbids.sql \
               p_insert_into_record_attribution_datazdbids.sql \
	       p_insert_marker_go_ev.sql p_populate_go_root_terms.sql \
	       p_check_drop_go_root_term.sql \
	       get_mrkr_abbrev_html.sql get_mrkr_name_html.sql p_update_clone_relationship.sql \

PUB_STATS    = get_pub_mini_ref.sql p_drop_go_root_term_attribution.sql \
	       pub_xpat_curation_status.sql jrnl_acknowledgment.sql \
	       get_pub_default_permissions.sql p_check_state_country.sql \
	       p_new_person_address.sql get_sortable_authors_lower.sql \
	       p_update_person_address.sql \
	       p_delete_curator_session.sql	\

REGEN_GNRCS  = regen_oevdisp.sql regen_genotype_display.sql regen_accession.sql\
	       regen_panelcount.sql regen_maps.sql regen_feature_ao_fast_search.sql regen_term.sql regen_pheno_term.sql
REGEN_STATS  = grab_zdb_flag.sql release_zdb_flag.sql add_ab_ao_fast_search.sql create_all_terms_contains.sql
ZDB_GNRCS    = table_exists.sql  
ZDB_AGGR    = concatenate.sql
ZDB_STATS    = get_obj_name.sql get_obj_abbrev.sql get_date_from_id.sql \
	       p_check_zdb_object_table.sql syscall.sql zdb_date.sql \
	       p_check_submitter_is_root.sql set_session_params.sql \
	       getWebexplode.sql get_curator_session_value.sql
# Some of the stage functions call other stage functions.  Therefore
# the order they are listed matters.
STAGE_STATS  = create_stg_name_ext.sql create_stg_name_long.sql \
	       get_stg_name_html.sql \
	       stg_window_consistent.sql \
	       anatitem_overlaps_stg_window.sql aoterm_overlaps_stg_window.sql \
	       p_check_anatitem_overlaps_stg_window.sql \
	       stg_windows_overlap.sql \
	       p_stg_hours_consistent.sql p_fimg_overlaps_stg_window.sql \
	       p_check_anatrel_stg_consistent.sql \
	       p_check_anat_anatrel_stg_consistent.sql
ANAT_STATS = create_anatomy_relationship_list.sql xpat_annot_adjust.sql \
	get_entity_name_html.sql get_postcomposed_term_html.sql regen_anatomy_counts.sql \
	get_phenotype_link_html.sql get_phenotype_popup_link_html.sql
# insert_clone, regen_anatomy.sql and get_genofeat_zgy_html_display.sql
# are special cases.  They contain 
# multiple procedures/functions in a single file.  We thought about splitting 
# these into a single file per function, but the functions are tightly
# coupled with the function the file is name after.  There was more to gain by
# leaving them all in one file, then by splitting them up.
MULTIPLE_PROCS_STATIC = insert_clone.sql clone_2_sanger.sql \
		 get_genofeat_mp_zyg_html_display.sql

MULTIPLE_PROCS_GENERIC = regen_anatomy.sql regen_term.sql 
GENERIC_AGGR = $(ZDB_AGGR) 
GENERIC_PROCS = $(ZDB_GNRCS) $(REGEN_GNRCS) \
		get_mutants_html_link.sql get_mrkr_url.sql	         	
STATIC_PROCS  = $(DBLINK_STATS) $(FISH_STATS) $(MRKR_STATS) $(PUB_STATS) \
		$(REGEN_STATS) $(ZDB_STATS) $(STAGE_STATS) $(ANAT_STATS) \
		$(LOAD_STATS) $(XPAT_STATS) \
		concat_init.sql concat_iter.sql concat_combine.sql concat_final.sql \
		get_mrkr_name_html_link.sql get_mrkr_abbrev_html_link.sql \
		get_geno_name_html_link.sql get_geno_name_with_bg_html_link.sql \
		get_genotype_backgrounds_html_link.sql  get_genofeat_mp_zyg_html_display.sql

SPECIFIC_PROC_TARGETS = $(foreach SPEC, $(GENERIC_PROCS), $(TARGETDIR)/$(SPEC))
STATIC_PROC_TARGETS   = $(foreach STAT, $(STATIC_PROCS),  $(TARGETDIR)/$(STAT))
MULTIPLE_PROC_TARGETS_G = $(foreach SPEC, $(MULTIPLE_PROCS_GENERIC), $(TARGETDIR)/$(SPEC))
MULTIPLE_PROC_TARGETS_S = $(foreach SPEC, $(MULTIPLE_PROCS_STATIC), $(TARGETDIR)/$(SPEC))
SPECIFIC_AGGR_TARGETS = $(foreach SPEC, $(GENERIC_AGGR), $(TARGETDIR)/$(SPEC))

ENDEMICTARGETS_POSTTARGETS = $(SPECIFIC_PROC_TARGETS) $(STATIC_PROC_TARGETS) \
			     $(MULTIPLE_PROC_TARGETS) $(SPECIFIC_AGGR_TARGETS) 

# ---------------  Production Rules  ------------------------------------

include $(TOP)/make.default.rules

$(SPECIFIC_PROC_TARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@
	-echo 'drop procedure $(patsubst %.sql,%,$^)' | $(DBACCESS) $(DBNAME) > /dev/null 2>&1
	$(DBACCESS) $(DBNAME) $@
	echo 'update statistics for procedure $(patsubst %.sql,%,$^)' | $(DBACCESS) $(DBNAME)

$(STATIC_PROC_TARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	-echo 'drop procedure $(patsubst %.sql,%,$^)' | $(DBACCESS) $(DBNAME) > /dev/null 2>&1
	$(DBACCESS) $(DBNAME) $@
	echo 'update statistics for procedure $(patsubst %.sql,%,$^)' | $(DBACCESS) $(DBNAME)

$(MULTIPLE_PROC_TARGETS_G) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@
	-$(DBACCESS) $(DBNAME) $@

$(MULTIPLE_PROC_TARGETS_S) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@

	-$(DBACCESS) $(DBNAME) $@

$(SPECIFIC_AGGR_TARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	-echo 'drop aggregate $(patsubst %.sql,%,$^)' | $(DBACCESS) $(DBNAME) > /dev/null 2>&1
	$(DBACCESS) $(DBNAME) $@

