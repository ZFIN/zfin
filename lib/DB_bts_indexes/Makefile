#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Database bts indexes library
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Every bts index that is defined on ZFIN tables is defined in its own 
# file in this directory, or its subdirectories.
# Each file creates the index.  
# Files *must* have the same name as the index they define, appended by sql.
# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_bts_indexes

INDEXES = fish_annotation_search_fas_all_bts_index.sql figure_term_fish_search_term_group_bts_index.sql

ENDEMICTARGETS_POSTTARGETDIR = $(foreach IDX, $(INDEXES), $(TARGETDIR)/$(IDX))

# ---------------  Production Rules  ------------------------------------

include ../../make.default.rules

$(ENDEMICTARGETS_POSTTARGETDIR) : $(TARGETDIR)/% : %
	-echo "drop index $(patsubst %.sql,%,$^)" | $(DBACCESS) $(DBNAME) > /dev/null 2>&1
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(DBACCESS) $(DBNAME) $@


# ---------------  Special Rules  ---------------------------------------

postloaddb : clobber all
