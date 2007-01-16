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

DIR = lib/DB_functions/SPL/Regen_names
TARGETDIR = $(TARGETROOT)/$(DIR)
TESTDIR = /tmp/$(DBNAME)/$(DIR)

STATIC_PROCS = regen_names_finish.sql regen_names_create_temp_tables.sql \
               regen_names_drop_dups.sql regen_name_ends_list.sql \
               regen_names_feature_list.sql regen_names_feature.sql \
               regen_names_marker_list.sql regen_names_marker.sql \
               regen_names_genotype.sql regen_names_genotype_list.sql  
GENERIC_PROCS = regen_names.sql

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

# TEST REGEN_NAMES()
# 
# Due to the size of the tables, these commands take a long time to run.
# Exclude serial column from unlaods.  This will change from run to run.

testpre : 
	-mkdir -p $(TESTDIR)
	echo ' \
  unload to "$(TESTDIR)/all_map_names.pre" \
    select allmapnm_name, allmapnm_zdb_id, allmapnm_significance, \
           allmapnm_precedence, allmapnm_name_lower \
      from all_map_names \
      order by allmapnm_zdb_id, allmapnm_significance, allmapnm_precedence, \
               allmapnm_name' | $(DBACCESS) $(DBNAME)
	echo ' \
  unload to "$(TESTDIR)/all_name_ends.pre" \
    select allmapnm_zdb_id, allnmend_name_end_lower, allmapnm_significance \
      from all_map_names, all_name_ends \
      where allmapnm_serial_id = allnmend_allmapnm_serial_id \
      order by allmapnm_zdb_id, allmapnm_significance, allmapnm_precedence, \
               allmapnm_name' | $(DBACCESS) $(DBNAME)

testpost :
	echo ' \
  unload to "$(TESTDIR)/all_map_names.post" \
    select allmapnm_name, allmapnm_zdb_id, allmapnm_significance, \
           allmapnm_precedence, allmapnm_name_lower \
      from all_map_names \
      order by allmapnm_zdb_id, allmapnm_significance, allmapnm_precedence, \
               allmapnm_name' | $(DBACCESS) $(DBNAME)
	echo ' \
  unload to "$(TESTDIR)/all_name_ends.post" \
    select allmapnm_zdb_id, allnmend_name_end_lower, allmapnm_significance \
      from all_map_names, all_name_ends \
      where allmapnm_serial_id = allnmend_allmapnm_serial_id \
      order by allmapnm_zdb_id, allmapnm_significance, allmapnm_precedence, \
               allmapnm_name' | $(DBACCESS) $(DBNAME)
	diff $(TESTDIR)/all_map_names.pre $(TESTDIR)/all_map_names.post
	diff $(TESTDIR)/all_name_ends.pre $(TESTDIR)/all_name_ends.post


# TEST REGEN_NAMES_FEATURE, REGEN_NAMES_GENOTYPE, REGEN_NAMES_MARKER
#
# These expect a single ZDB ID as input.  The ZDB ID to use for a particular
# test is passed into the makefile as follows:
#
#  gmake testprezdbid  ZDB_ID=ZDB-GENE-951212-1
#  gmake testmarker    ZDB_ID=ZDB-GENE-951212-1
#  gmake testpostzdbid ZDB_ID=ZDB-GENE-951212-1
#

testprezdbid :
	-mkdir -p $(TESTDIR)
	echo ' \
  unload to "$(TESTDIR)/all_map_names.$(ZDB_ID).pre" \
    select allmapnm_name, allmapnm_zdb_id, allmapnm_significance, \
           allmapnm_precedence, allmapnm_name_lower \
      from all_map_names \
      where allmapnm_zdb_id = "$(ZDB_ID)" \
      order by allmapnm_zdb_id, allmapnm_significance, allmapnm_precedence, \
               allmapnm_name' | $(DBACCESS) $(DBNAME)
	echo ' \
  unload to "$(TESTDIR)/all_name_ends.$(ZDB_ID).pre" \
    select allmapnm_zdb_id, allnmend_name_end_lower, allmapnm_significance \
      from all_map_names, all_name_ends \
      where allmapnm_serial_id = allnmend_allmapnm_serial_id \
        and allmapnm_zdb_id = "$(ZDB_ID)" \
      order by allmapnm_zdb_id, allnmend_name_end_lower, \
               allmapnm_significance' | $(DBACCESS) $(DBNAME)


testfeature :
	echo 'execute procedure regen_names_feature("$(ZDB_ID)")' | $(DBACCESS) $(DBNAME)

testgenotype :
	echo 'execute procedure regen_names_genotype("$(ZDB_ID)")' | $(DBACCESS) $(DBNAME)

testmarker :
	echo 'execute procedure regen_names_marker("$(ZDB_ID)")' | $(DBACCESS) $(DBNAME)


testpostzdbid :
	-mkdir -p $(TESTDIR)
	echo ' \
  unload to "$(TESTDIR)/all_map_names.$(ZDB_ID).post" \
    select allmapnm_name, allmapnm_zdb_id, allmapnm_significance, \
           allmapnm_precedence, allmapnm_name_lower \
      from all_map_names \
      where allmapnm_zdb_id = "$(ZDB_ID)" \
      order by allmapnm_zdb_id, allmapnm_significance, allmapnm_precedence, \
               allmapnm_name' | $(DBACCESS) $(DBNAME)
	echo ' \
  unload to "$(TESTDIR)/all_name_ends.$(ZDB_ID).post" \
    select allmapnm_zdb_id, allnmend_name_end_lower, allmapnm_significance \
      from all_map_names, all_name_ends \
      where allmapnm_serial_id = allnmend_allmapnm_serial_id \
        and allmapnm_zdb_id = "$(ZDB_ID)" \
      order by allmapnm_zdb_id, allnmend_name_end_lower, \
               allmapnm_significance' | $(DBACCESS) $(DBNAME)
	diff $(TESTDIR)/all_map_names.$(ZDB_ID).pre \
             $(TESTDIR)/all_map_names.$(ZDB_ID).post
	diff $(TESTDIR)/all_name_ends.$(ZDB_ID).pre \
             $(TESTDIR)/all_name_ends.$(ZDB_ID).post
