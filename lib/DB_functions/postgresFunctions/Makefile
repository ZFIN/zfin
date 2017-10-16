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


TARGETDIR = $(TARGETROOT)/lib/DB_functions/postgresFunctions
SUBDIRS = Regen_genox

ZDB_STATS = regen_panelcount.sql regen_construct_marker.sql jrnl_acknowledgment.sql p_check_zdb_object_table.sql p_update_related_fish_name.sql p_update_fish_name.sql p_drop_go_root_term_attribution.sql p_check_submitter_is_root.sql p_stg_hours_consistent.sql p_feature_stats_check_constraints.sql p_fimg_overlaps_stg_window.sql p_update_related_genotype_names.sql p_delete_curator_session.sql p_dblink_has_parent.sql p_check_zdb_id_in_genedom.sql p_check_tginsertion_has_construct_relationship.sql p_check_state_country.sql p_check_pato_postcomposed_terms.sql p_check_pato_entities.sql p_check_mrkr_abbrev.sql p_check_fx_postcomposed_terms.sql p_check_EFG_WT_expression.sql p_check_drop_go_root_term.sql p_check_caps_acc_num.sql p_check_anatrel_stg_consistent.sql p_check_anatitem_overlaps_stg_window.sql p_check_anat_anatrel_stg_consistent.sql p_2_linkage_pairs.sql p_fmrel_grpmem_correct.sql p_goterm_not_obsolete.sql p_insert_clone_from_marker.sql p_insert_into_record_attribution_datazdbids.sql p_insert_into_record_attribution_tablezdbids.sql p_insert_marker_go_ev.sql p_marker_has_goterm.sql p_mrel_grpmem_correct.sql p_term_is_not_obsolete_or_secondary.sql p_update_BurgessLinn_genotype_names.sql p_update_dblink_length.sql p_update_clone_relationship.sql p_populate_go_root_terms.sql p_set_fish_to_modified.sql p_update_fmrel_genotype_names.sql p_update_related_fish_for_str.sql p_update_geno_nickname.sql p_update_related_fish_names.sql p_update_related_names.sql p_update_unspecified_alleles.sql restrictGAFEntries.sql get_date_from_id.sql get_ao_name_html_link.sql get_curator_session_value.sql get_dblink_acc_num_display.sql get_feature_abbrev_display.sql get_feature_type.sql get_fish_full_name.sql get_fish_handle.sql get_fish_name.sql getFishOrder.sql get_genotype_backgrounds.sql get_genotype_handle.sql get_id.sql get_obj_abbrev.sql get_obj_name.sql get_obj_type.sql nvl.sql scrub_char.sql zero_pad.sql add_ab_ao_fast_search.sql addSourceAlias.sql anatitem_overlaps_stg_window.sql setTscriptLoadId.sql updateMaxStatusAsCurrent.sql updatestandard.sql zdb_date.sql updatestandardorgenericcontrol.sql updatePubCompletionDate.sql updatePubLastSentEmailDate.sql updatePubIndexedDate.sql update_geno_sort_order.sql updateCurrentPubStatus.sql update_construct_name.sql update_construct_name_component.sql updateConstructAbbrev.sql update_component_text.sql updateAbbrevEqualName.sql table_exists.sql strip_omim.sql stg_windows_overlap.sql stg_window_consistent.sql  get_genotype_display.sql get_genotype_backgrounds_html_link.sql get_mrkr_url.sql get_postcomposed_term_html.sql get_postcomposed_term_text.sql get_pub_default_permissions.sql  get_sortable_authors_lower.sql get_stg_name_html.sql checkFeatureAbbrev.sql checkDblinkOneToOneAccessionMapping.sql checkDblinkTranscriptWithdrawn.sql checkDupFeaturePrefixLineDesignation.sql checkPubTrackingLocationOwner.sql check_xpat_null_valid.sql mhist_event.sql get_stg_name_long_html.sql getZfinAccessionNumberNoInsert.sql getZfinAccessionNumber.sql pub_xpat_curation_status.sql increment_candidate_occurrences.sql insert_clone.sql insrt_cln.sql aoterm_overlaps_stg_window.sql create_stg_name_ext.sql create_stg_name_long.sql create_anatomy_relationship_list.sql populate_lab_address_update_tracking.sql populate_feature_tracking.sql populate_direct_submission_date.sql checkTscriptType.sql get_pub_mini_ref.sql regen_feature_term_fast_search.sql regen_fish_components.sql regen_expression_mart_per_pub.sql regen_construct.sql populate_anat_display_stage_children.sql regen_anatomy.sql regen_anatomy_counts.sql regen_expression_term_fast_search.sql populate_all_term_contains.sql regen_term.sql regen_genotype_display.sql regen_accession.sql fhist_event.sql strip_all_triggers.sql dropFunctions.sql

STATIC_PROCS  = $(ZDB_STATS)

SPECIFIC_PROC_TARGETS = $(foreach SPEC, $(GENERIC_PROCS), $(TARGETDIR)/$(SPEC))
STATIC_PROC_TARGETS   = $(foreach STAT, $(STATIC_PROCS),  $(TARGETDIR)/$(STAT))
#MULTIPLE_PROC_TARGETS_G = $(foreach MPG, $(MULTIPLE_PROCS_GENERIC), $(TARGETDIR)/$(MPG))
MULTIPLE_PROC_TARGETS_S = $(foreach MPS, $(MULTIPLE_PROCS_STATIC), $(TARGETDIR)/$(MPS))
SPECIFIC_AGGR_TARGETS = $(foreach SPECAT, $(GENERIC_AGGR), $(TARGETDIR)/$(SPECAT))
ENDEMICTARGETS_POSTTARGETS = $(SPECIFIC_PROC_TARGETS) $(STATIC_PROC_TARGETS) \
			     $(MULTIPLE_PROC_TARGETS) $(SPECIFIC_AGGR_TARGETS) 

# ---------------  Production Rules  ------------------------------------

include $(TOP)/make.default.rules

$(SPECIFIC_PROC_TARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@
	$(PSQL) -d $(DBNAME) $@

$(STATIC_PROC_TARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(PSQL) -d $(DBNAME) -a -f $@

$(MULTIPLE_PROC_TARGETS_S) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(PSQL) -d $(DBNAME)-a -f $@

$(SPECIFIC_AGGR_TARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(PSQL) -d $(DBNAME) -a -f $@
