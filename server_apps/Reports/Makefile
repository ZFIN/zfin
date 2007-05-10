#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps reports directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# 
# Targets made by this makefile are used to generate database content
# statistic reports. 


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/Reports

SUBDIRS = ZGC Vega PATO GO


GENERICS = Count.pl
STATICS  =  


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

run : $(TARGETDIR)/Count.pl
	$(TARGETDIR)/Count.pl

