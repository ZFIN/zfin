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
	anatomy_items_insert_trigger.sql \
	anatomy_items_update_trigger.sql \
	anatomy_relationship_insert_trigger.sql \
	anatomy_relationship_update_trigger.sql \
	clone_problem_type_insert_trigger.sql \
	clone_problem_type_update_trigger.sql \
	data_alias_insert_trigger.sql \
	dalias_alias_update_trigger.sql \
	db_link_insert_trigger.sql \
	db_link_update_trigger.sql \
	experiment_insert_trigger.sql \
	experiment_update_trigger.sql \
	experiment_condition_insert_trigger.sql \
	experiment_condition_update_trigger.sql \
	expression_experiment_insert_trigger.sql \
	expression_experiment_update_trigger.sql \
	expression_pattern_infrastructure_insert_trigger.sql \
	expression_pattern_infrastructure_update_trigger.sql \
	expression_result_insert_trigger.sql \
	expression_result_update_trigger.sql \
	feature_stats_insert_trigger.sql \
	figure_insert_trigger.sql \
	figure_update_trigger.sql \
	feature_marker_relationship_insert_trigger.sql \
	feature_marker_relationship_ftr_update_trigger.sql \
	feature_marker_relationship_mrkr_update_trigger.sql \
	feature_insert_trigger.sql \
	feature_abbrev_update_trigger.sql \
	feature_name_update_trigger.sql \
	genotype_insert_trigger.sql \
	geno_display_name_update_trigger.sql \
	geno_handle_update_trigger.sql \
	image_stage_insert_trigger.sql \
	image_stage_update_trigger.sql \
	int_person_pub_insert_trigger.sql \
	int_person_pub_update_trigger.sql \
	journal_insert_trigger.sql \
	journal_update_trigger.sql \
	lab_insert_trigger.sql \
	lab_update_trigger.sql \
	linkage_pair_member_insert_trigger.sql \
	linkage_pair_member_update_trigger.sql \
	marker_insert_trigger.sql \
	marker_comments_update_trigger.sql \
	mrkr_abbrev_update_trigger.sql \
	mrkr_name_update_trigger.sql \
        marker_go_term_evidence_update_trigger.sql \
	marker_go_term_evidence_insert_trigger.sql \
	inference_group_member_insert_trigger.sql \
	inference_group_member_notes_update_trigger.sql \
	inference_group_member_inferred_from_update_trigger.sql \
	marker_relationship_insert_trigger.sql \
	marker_relationship_update_trigger.sql \
	new_person_address_update_trigger.sql \
	new_person_address_insert_trigger.sql \
	orthologue_evidence_display_insert_trigger.sql \
	orthologue_evidence_display_update_trigger.sql \
	orthologue_insert_trigger.sql \
	orthologue_update_trigger.sql \
	person_insert_trigger.sql \
	person_update_trigger.sql \
	person_address_update_trigger.sql \
	persaddress_insert_trigger.sql \
	persaddress_update_trigger.sql \
	publication_insert_trigger.sql \
	publication_update_trigger.sql \
	publication_completion_date_update_trigger.sql \
	record_attribution_insert_trigger.sql \
	run_candidate_insert_trigger.sql \
	stage_insert_trigger.sql \
	stage_update_trigger.sql \
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
