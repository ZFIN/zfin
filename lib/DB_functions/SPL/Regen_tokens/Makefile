#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SQL/SPL Regen Tokens procedures.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# There are several fast search tables that contain name data broken up into
# "tokens".  The scripts in this directory produce those tables.  Also see
# the ../Regen_names directory for some similar tables.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

DIR = lib/DB_functions/SPL/Regen_tokens
TARGETDIR = $(TARGETROOT)/$(DIR)
TESTDIR = /tmp/$(DBNAME)/$(DIR)

STATIC_PROCS = tokenize_init.sql tokenize_list.sql
GENERIC_PROCS = regen_anatomy_tokens.sql

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


# ---------------  Testing Rules  ---------------------------------------

# Testing in this directory consists of doing ordered unloads of tables
# both before and after implementing a change, and then comparing the results.

# TEST REGEN_ANATOMY_TOKENS()

testanatpre : 
	-mkdir -p $(TESTDIR)
	echo ' \
  unload to "$(TESTDIR)/all_anatomy_name_tokens.pre" \
    select anattok_token_lower, anattok_anatitem_zdb_id \
      from all_anatomy_name_tokens \
      order by anattok_anatitem_zdb_id, anattok_token_lower' | $(DBACCESS) $(DBNAME)


# post test gets a make error if the pre and post files are different.

testanatpost :
	echo ' \
  unload to "$(TESTDIR)/all_anatomy_name_tokens.post" \
    select anattok_token_lower, anattok_anatitem_zdb_id \
      from all_anatomy_name_tokens \
      order by anattok_anatitem_zdb_id, anattok_token_lower' | $(DBACCESS) $(DBNAME)
	diff $(TESTDIR)/all_anatomy_name_tokens.pre $(TESTDIR)/all_anatomy_name_tokens.post
