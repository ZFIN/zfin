#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, John Postlethwait Data Unload directory
#
# $Source$
# $Id$
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The johnp.pl script is run by cron every week.  It invokes the sql script.
# The johnp dump uses the FTP directory structure.  See the 
# ZFIN Documentation for how FTP is handled differently in test versus 
# production.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/JohnP

GENERICS = johnp.pl create_files_for_johnp.sql


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run the Johnp DB dump script.  Note that this may not produce the files
# with world readable permissions (but when run from cron it does).

run : $(TARGETDIR)/johnp.pl
	$(TARGETDIR)/johnp.pl

