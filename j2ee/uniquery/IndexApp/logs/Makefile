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

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/j2ee/uniquery/IndexApp/logs

# I orginally left these 2 scripts in the source directory (this directory)
# because they can be run without ill effects from this directory.  Also, 
# it made sense to me that the logs dir should only contain log files.
# However, when I wrote the cron job to run these, I just assumed that the
# scripts were in the target directory, causing the cron job to fail.  On
# further thought, I decided it is bad practice to run scripts out of the
# source tree, and we should copy everything and run everything out of the
# target tree.  So, now we do.

STATICS = check_for_duplicates.pl count_pages.pl


# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules


# ---------------  Misc Rules  ------------------------------------

# Check crawled log for dups
checkdupscrawl : $(TARGETDIR)/check_for_duplicates.pl
	$(TARGETDIR)/check_for_duplicates.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/crawledUrls.log

# Check indexed log for dups
checkdupsindex : $(TARGETDIR)/check_for_duplicates.pl
	$(TARGETDIR)/check_for_duplicates.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/indexedUrls.log


# Count crawled pages
countcrawl : $(TARGETDIR)/count_pages.pl
	$(TARGETDIR)/count_pages.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/crawledUrls.log

# Count indexed pages
countindex : $(TARGETDIR)/count_pages.pl
	$(TARGETDIR)/count_pages.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/indexedUrls.log
