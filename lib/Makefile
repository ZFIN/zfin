#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, lib directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib

SUBDIRS = DB_functions DB_triggers Java
POSTLOADDB_SUBDIRS = DB_functions DB_triggers

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Special Targets  -------------------------------------

postloaddb :
	$(foreach PLDIR,$(POSTLOADDB_SUBDIRS), $(MAKE) -C $(PLDIR) $@; )
