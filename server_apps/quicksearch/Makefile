#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  IndexApp directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The IndexApp crawls the website using Lucene and builds an index of 
# what terms occur in what pages.  The indexer is run by a cron job.
# It is not accessable from the web.  See SearchApp for how the indexes
# actually get used.


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/quicksearch

GENERICS = regenerateindex.pl makeStaticIndex.pl

SUBDIRS = logs etc 


# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules



# ---------------  Misc Rules  ------------------------------------

run : $(TARGETDIR)/regenerateindex.pl
	cd $(TARGETDIR); ./regenerateindex.pl
