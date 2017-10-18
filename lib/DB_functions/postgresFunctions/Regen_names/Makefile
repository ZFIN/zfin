#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SQL/SPL Regen_names procedures.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The routines defined in this directory are for regenerating the 
# all_map_names and all_name_ends tables.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

DIR = lib/DB_functions/postgresFunctions/Regen_names
TARGETDIR = $(TARGETROOT)/$(DIR)
TESTDIR = /tmp/$(DBNAME)/$(DIR)

STATIC_PROCS = regen_names_finish.sql regen_names_create_temp_tables.sql \
               regen_names_drop_dups.sql regen_name_ends_list.sql \
               regen_names_feature_list.sql regen_names_feature.sql \
               regen_names_marker_list.sql regen_names_marker.sql \
               regen_names_genotype.sql regen_names_genotype_list.sql  \
	       regen_names_fish.sql regen_names_fish_list.sql  
GENERIC_PROCS = regen_names.sql

SPECIFIC_PROC_TARGETS = $(foreach SPEC, $(GENERIC_PROCS), $(TARGETDIR)/$(SPEC))
STATIC_PROC_TARGETS   = $(foreach STAT, $(STATIC_PROCS),  $(TARGETDIR)/$(STAT))

ENDEMICTARGETS_POSTTARGETS = $(SPECIFIC_PROC_TARGETS) $(STATIC_PROC_TARGETS)

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
