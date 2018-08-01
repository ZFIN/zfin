#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, ReNo run load directory
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

SUBDIRS = Nomenclature

GENERICS = load_run_report_hit.sh

STATICS =  write-run-report-hit.r load_run_report_hit.sql \
	   commit.sql rollback.sql addTemporaryEnsdarps.sql dropTemporaryEnsdarps.sql
## provide a default run name so developers can copy or symlink a file
# and do: gmake run
# or 
# or they can provide a filename:  gmake CTX=<filename> run

RUNCTX = run.ctx

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Misc Targets  ----------------------------------------
$(RUNCTX):
	$(error "need to provide the run.ctx file")

run:   $(RUNCTX)
	./write-run-report-hit.r $(RUNCTX) 
	cp -f *.unl $(TARGETDIR)
	$(TARGETDIR)/load_run_report_hit.sh
# 	$(error "\
# 	dbaccess -a $$DBNAME << END\
# 	drop table tmp_run;\
# 	drop table tmp_report;\
# 	drop table tmp_hit;\
# 	drop table tmp_candidate;\
# 	drop table tmp_run_cnd;\
# 	drop table tmp_blast_query;\
# 	END\
# 	")
#	rm -f *.unl



run_commit: $(RUNCTX)
	./write-run-report-hit.r $(RUNCTX)
	cp -f *.unl $(TARGETDIR)
	$(TARGETDIR)/load_run_report_hit.sh commit
#	rm -f *.unl
#	rm -f run.ctx

