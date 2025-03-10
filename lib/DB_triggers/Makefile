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

TARGETDIR = $(TARGETROOT)/lib/DB_triggers

ZDB_STATS = company.sql publication.sql zdb_submitters.sql zdb_object_type.sql updates.sql transcript.sql term.sql term_stage.sql term_relationship.sql stage.sql source_alias.sql run_candidate.sql record_attribution.sql pub_tracking_history.sql publication_completion_date.sql pub_correspondence_sent_tracker.sql phenotype_statement.sql phenotype_experiment.sql person.sql one_to_one_accession.sql marker_name.sql marker_abbrev.sql marker_sequence.sql marker_relationship.sql marker.sql marker_go_term_evidence.sql linkage.sql lab.sql lab_address.sql journal.sql int_person_pub.sql inference_group_member.sql image_stage.sql fish_experiment.sql genotype_display_name.sql genotype_handle.sql genotype.sql genotype_feature.sql fish_str.sql figure.sql anatomy_display.sql clone.sql construct_component.sql construct.sql data_alias.sql db_link.sql disease_annotation.sql experiment.sql expression_experiment2.sql expression_pattern_infrastructure.sql external_reference.sql feature_name.sql feature_abbrev.sql feature.sql fish.sql feature_marker_relationship.sql feature_marker_relationship_feature.sql feature_marker_relationship_marker.sql marker_audit.sql

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
