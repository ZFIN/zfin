#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SQL/SPL Regen_genox procedures.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The routines defined in this directory are for regenerating the 
# mutant_fast_search table.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

DIR = lib/DB_functions/SPL/Regen_genox
TARGETDIR = $(TARGETROOT)/$(DIR)
TESTDIR = /tmp/$(DBNAME)/$(DIR)

STATIC_PROCS = regen_genox_finish.sql regen_genox_create_temp_tables.sql \
               regen_genox_process.sql regen_genox_marker.sql  \
               regen_genofig_clean_exp.sql regen_genofig_create_temp_tables.sql \
               regen_genofig_finish.sql regen_genofig_process.sql \
               regen_genofig_genotype.sql regen_genofig_geno_finish.sql regen_genofig_process_indiv.sql
GENERIC_PROCS = regen_genox.sql

SPECIFIC_PROC_TARGETS = $(foreach SPEC, $(GENERIC_PROCS), $(TARGETDIR)/$(SPEC))
STATIC_PROC_TARGETS   = $(foreach STAT, $(STATIC_PROCS),  $(TARGETDIR)/$(STAT))

ENDEMICTARGETS_POSTTARGETS = $(SPECIFIC_PROC_TARGETS) $(STATIC_PROC_TARGETS)

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
