#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, GenBank daily update file automated load,
# ZFIN GenBank accession information auto update
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# See the README file. 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Genbank


GENERICS = gbaccession.pl gbaccession_PG.pl

STATICS = parseDaily.pl GenBank-Accession-Update_d.sql parseDaily_PG.pl GenBank-Accession-Update_d_PG.sql README


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

run :
	$(TARGETDIR)/gbaccession.pl

