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
# The MEOW DB dump uses the FTP directory structure.  See the 
# ZFIN Documentation for how FTP is handled differently in test versus 
# production.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/ZGC

GENERICS = zgc.pl zLib.pl

STATICS = load_zgc.sql zgc.sed StaticLibList.unl

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run the download script.  This extracts data from ZFIN and places it in 
# the public ftp directory

#run : $(TARGETDIR)/zgc.pl 
#	$(TARGETDIR)/zgc.pl

