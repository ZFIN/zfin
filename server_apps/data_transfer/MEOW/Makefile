#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, MEOW DB Data Unload directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The meow.pl script is run by Jenkins every week.  It invokes the sql script.
# The MEOW DB dump uses the FTP directory structure.  See the 
# ZFIN Documentation for how FTP is handled differently in test versus 
# production.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/MEOW

GENERICS = meow.pl meow_PG.pl
STATICS = MEOW_dump.sql MEOW_dump_PG.sql

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run the MEOW DB dump script.  Note that this may not produce the files
# with world readable permissions (but when run from cron it does).

run : $(TARGETS)
	$(TARGETDIR)/meow.pl

