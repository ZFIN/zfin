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
	alteration_allele_update_trigger.sql \
	alteration_insert_trigger.sql \
	anatomy_display_insert_trigger.sql \
	anatomy_display_update_trigger.sql \
	anatomy_items_insert_trigger.sql \
	anatomy_items_update_trigger.sql \
	anatomy_relationship_insert_trigger.sql \
	anatomy_relationship_update_trigger.sql \
	column_attribution_insert_trigger.sql \
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
	figure_insert_trigger.sql \
	figure_update_trigger.sql \
	fish_allele_update_trigger.sql \
	fish_image_anatomy_insert_trigger.sql \
	fish_image_anatomy_update_trigger.sql \
	fish_image_stage_insert_trigger.sql \
	fish_image_stage_update_trigger.sql \
	fish_insert_trigger.sql \
	fish_name_update_trigger.sql \
	fish_pheno_keywords_update_trigger.sql \
	journal_insert_trigger.sql \
	journal_update_trigger.sql \
	lab_insert_trigger.sql \
	lab_update_trigger.sql \
	linkage_pair_member_insert_trigger.sql \
	linkage_pair_member_update_trigger.sql \
        locus_update_trigger.sql \
	locus_insert_trigger.sql \
	locus_registration_insert_trigger.sql \
	locusreg_abbrev_update_trigger.sql \
	locusreg_name_update_trigger.sql \
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
	publication_insert_trigger.sql \
	publication_update_trigger.sql \
	record_attribution_insert_trigger.sql \
	stage_insert_trigger.sql \
	stage_update_trigger.sql \
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
