#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Server apps, web site tools directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This directory contains tools for analyzing / checking the web site.
# This includes a web page link checker that is run weekly, and a 
# signs of life script that is run every few minutes.


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/WebSiteTools

GENERICS = signsoflife.sh \
           webrotate.pl gen-jobs-rss.r gen-meetings-rss.r gen-news-rss.r

STATICS = makemirror.csh domainlookup.pl rss-util.r

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run the script that updates the mirror.zfin.org web site.

runmakemirror: $(TARGETDIR)/makemirror.csh
	$(TARGETDIR)/makemirror.csh

runsignsoflife : $(TARGETDIR)/signsoflife.sh
	$(TARGETDIR)/signsoflife.sh
