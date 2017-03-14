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


# Group the SPLs according to these characteristcs
# 1. General area the SPL is for.  This is basically a comment.
# 2. Generic or static.  The makefile needs to know this.

ZDB_STATS    = get_obj_name.sql get_obj_abbrev.sql get_date_from_id.sql \
	       p_check_zdb_object_table.sql syscall.sql zdb_date.sql \
	       p_check_submitter_is_root.sql set_session_params.sql \
	       getWebexplode.sql get_curator_session_value.sql

# ---------------  Production Rules  ------------------------------------

include $(TOP)/make.default.rules

$(SPECIFIC_PROC_TARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@
	-echo 'drop function $(patsubst %.sql,%,$^)' | $(PSQL) $(DBNAME) > /dev/null 2>&1
	$(PSQL) $(DBNAME) $@

$(STATIC_PROC_TARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	-echo 'drop function $(patsubst %.sql,%,$^)' | $(PSQL) $(DBNAME) > /dev/null 2>&1
	$(PSQL) $(DBNAME) $@

$(MULTIPLE_PROC_TARGETS_S) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(PSQL) $(DBNAME) $@

