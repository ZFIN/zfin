#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Database triggers library
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Every trigger that is defined on ZFIN tables is defined in its own 
# file in this directory, or its subdirectories.
# Each file creates the trigger.  
# Files *must* have the same name as the trigger they define.
# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_triggers

TRIGGERS = \
	anatomy_display_insert_trigger.sql \
	anatomy_display_update_trigger.sql \
	clone_problem_type_insert_trigger.sql \
	clone_problem_type_update_trigger.sql \
	construct_insert_trigger.sql \
	construct_name_update_trigger.sql \
	construct_component_update_trigger.sql \
	data_alias_insert_trigger.sql \
	dalias_alias_update_trigger.sql \
	db_link_insert_trigger.sql \
	db_link_update_trigger.sql \
	disease_annotation_insert_trigger.sql \
	disease_annotation_update_trigger.sql \
	external_reference_insert_trigger.sql \
	experiment_insert_trigger.sql \
	experiment_update_trigger.sql \
	expression_experiment2_insert_trigger.sql \
	expression_experiment2_update_trigger.sql \
	expression_pattern_infrastructure_insert_trigger.sql \
	expression_pattern_infrastructure_update_trigger.sql \
	figure_insert_trigger.sql \
	figure_update_trigger.sql \
	feature_marker_relationship_insert_trigger.sql \
	feature_marker_relationship_ftr_update_trigger.sql \
	feature_marker_relationship_mrkr_update_trigger.sql \
	feature_insert_trigger.sql \
	feature_abbrev_update_trigger.sql \
	feature_name_update_trigger.sql \
	fish_insert_trigger.sql \
	fish_name_update_trigger.sql \
	fish_str_insert_trigger.sql \
	fish_str_modified_trigger.sql \
	genotype_insert_trigger.sql \
	genofeat_insert_trigger.sql \
	genofeat_update_trigger.sql \
	geno_display_name_update_trigger.sql \
	geno_handle_update_trigger.sql \
	genox_exp_update_trigger.sql \
	genox_exp_insert_trigger.sql \
	image_stage_insert_trigger.sql \
	image_stage_update_trigger.sql \
	int_person_pub_insert_trigger.sql \
	int_person_pub_update_trigger.sql \
	journal_insert_trigger.sql \
	journal_update_trigger.sql \
	lab_insert_trigger.sql \
	lab_update_trigger.sql \
	lab_address_update_trigger.sql \
	marker_insert_trigger.sql \
	marker_comments_update_trigger.sql \
	mrkr_abbrev_update_trigger.sql \
        mrkr_name_update_trigger.sql \
	marker_go_term_evidence_update_trigger.sql \
	marker_go_term_evidence_insert_trigger.sql \
	marker_sequence_insert_trigger.sql \
	marker_sequence_update_trigger.sql \
	inference_group_member_insert_trigger.sql \
	inference_group_member_notes_update_trigger.sql \
	inference_group_member_inferred_from_update_trigger.sql \
	linkage_insert_trigger.sql \
	linkage_update_trigger.sql \
	marker_relationship_insert_trigger.sql \
	marker_relationship_mrkr_1_update_trigger.sql \
	marker_relationship_mrkr_2_update_trigger.sql \
	marker_relationship_type_update_trigger.sql \
	marker_relationship_update_trigger.sql \
	one_to_one_accession_insert_trigger.sql \
	one_to_one_accession_update_trigger.sql \
	person_insert_trigger.sql \
	person_update_trigger.sql \
	phenotype_experiment_insert_trigger.sql \
	phenotype_experiment_update_trigger.sql \
	phenotype_statement_insert_trigger.sql \
	phenotype_statement_update_trigger.sql \
	publication_insert_trigger.sql \
	publication_update_trigger.sql \
	publication_completion_date_update_trigger.sql \
	pub_tracking_history_insert_trigger.sql \
	record_attribution_insert_trigger.sql \
	run_candidate_insert_trigger.sql \
	stage_insert_trigger.sql \
	stage_update_trigger.sql \
	term_insert_trigger.sql \
	term_update_trigger.sql \
	term_relationship_insert_trigger.sql \
	term_relationship_update_trigger.sql \
	term_stage_insert_trigger.sql \
	term_stage_update_trigger.sql \
	transcript_insert_trigger.sql \
	transcript_update_trigger.sql \
	updates_insert_trigger.sql \
	zdb_object_type_insert_trigger.sql \
	zdb_object_type_update_trigger.sql \
	zdb_submitters_insert_trigger.sql

ENDEMICTARGETS_POSTTARGETDIR = $(foreach TRIG, $(TRIGGERS), $(TARGETDIR)/$(TRIG))

# ---------------  Production Rules  ------------------------------------

include ../../make.default.rules

$(ENDEMICTARGETS_POSTTARGETDIR) : $(TARGETDIR)/% : %
	-echo "drop trigger $(patsubst %.sql,%,$^)" | $(DBACCESS) $(DBNAME) > /dev/null 2>&1
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(DBACCESS) $(DBNAME) $@


# ---------------  Special Rules  ---------------------------------------

postloaddb : clobber all
