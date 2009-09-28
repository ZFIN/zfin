#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, ZGC automated data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The zgcClone script is run by cron daily.  It invokes the sql script.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/ZGC

GENERICS = zgc.pl 

STATICS = zgc.sed StaticLibList.unl zgc_pre_load_preparation.sql \
	  zgc_post_load_cleanup.sql zLib.pl cleanup_zgc.sql

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run script to create ZGC links in the database

run : $(TARGETS)
	$(TARGETDIR)/zgc.pl

