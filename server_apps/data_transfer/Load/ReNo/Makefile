#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, GenPept automated data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Load/ReNo

SUBDIRS =

GENERICS = load_run_report_hit.sh

STATICS =  write-run-report-hit.r load_run_report_hit.sql

RUN_CTX = ls -1t *.ctx|head -1

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Misc Targets  ----------------------------------------

run	:
	write-run-report-hit.r run.ctx 
	cp -f *.unl $(TARGETDIR)
	$(TARGETDIR)/load_run_report_hit.sh
	rm -f *.unl
	rm -f run.ctx

