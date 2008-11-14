#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  IndexApp/logs directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile creates the subdirectory that holds the indexer logs.
# It also contains scripts that analyze the logs.  The scripts are also
# copied to the log directory.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/quicksearch/logs

STATICS = check_for_duplicates.pl count_pages.pl


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules


# ---------------  Misc Rules  ------------------------------------

# Check crawled log for dups
checkdupscrawl : $(TARGETDIR)/check_for_duplicates.pl
	$(TARGETDIR)/check_for_duplicates.pl $(TARGETROOT)/server_apps/quicksearch/logs/crawledUrls.log

# Check indexed log for dups
checkdupsindex : $(TARGETDIR)/check_for_duplicates.pl
	$(TARGETDIR)/check_for_duplicates.pl $(TARGETROOT)/server_apps/quicksearch/logs/indexedUrls.log


# Count crawled pages
countcrawl : $(TARGETDIR)/count_pages.pl
	$(TARGETDIR)/count_pages.pl $(TARGETROOT)/server_apps/quicksearch/logs/crawledUrls.log

# Count indexed pages
countindex : $(TARGETDIR)/count_pages.pl
	$(TARGETDIR)/count_pages.pl $(TARGETROOT)/server_apps/quicksearch/logs/indexedUrls.log
