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

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_functions/Regen_genox

ZDB_STATS = regen_genox.sql regen_genox_create_temp_tables.sql regen_genox_process_marker.sql regen_genox_finish_marker.sql regen_genofig_create_temp_tables.sql regen_genofig_process.sql regen_genofig_finish.sql regen_genox_process_genox.sql regen_genox_fish_experiment.sql regen_genox_finish_genox.sql regen_genox_marker.sql regen_genofig_phenox.sql regen_genox_genox.sql

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
