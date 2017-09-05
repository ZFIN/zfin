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
# clean_expression_fast_search table.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

DIR = lib/DB_functions/postgresFunctions/regenCleanExpression
TARGETDIR = $(TARGETROOT)/$(DIR)
TESTDIR = /tmp/$(DBNAME)/$(DIR)

STATIC_PROCS= regen_clean_expression_create_temp_tables.sql \
		regen_clean_expression_process.sql regen_clean_expression.sql
GENERIC_PROCS=

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
