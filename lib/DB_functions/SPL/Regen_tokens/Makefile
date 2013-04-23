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
GENERIC_PROCS = regen_alias_tokens.sql

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

runanat :
	/bin/date
	echo 'execute function regen_anatomy_tokens()' | $(DBACCESS) $(DBNAME)
	/bin/date

# TEST REGEN_ALIAS_TOKENS()

runalias :
	/bin/date
	echo 'execute function regen_alias_tokens()' | $(DBACCESS) $(DBNAME)
	/bin/date

testaliaspre : 
	-mkdir -p $(TESTDIR)
	echo ' \
  unload to "$(TESTDIR)/all_alias_tokens.pre" \
    select aliastok_token_lower, aliastok_dalias_zdb_id \
      from all_alias_tokens \
      order by aliastok_dalias_zdb_id, aliastok_token_lower' | $(DBACCESS) $(DBNAME)

testaliaspost :
	echo ' \
  unload to "$(TESTDIR)/all_alias_tokens.post" \
    select alias_token_lower, aliastok_dalias_zdb_id \
      from all_alias_tokens \
      order by aliastok_dalias_zdb_id, aliastok_token_lower' | $(DBACCESS) $(DBNAME)
	-/bin/diff $(TESTDIR)/all_alias_tokens.pre $(TESTDIR)/all_alias_tokens.post