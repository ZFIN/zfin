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
# copied to the log directory.  The scripts do not contain any dynamic tags, 
# so the copies in this directory or the copies in the destination directory
# can be run.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/j2ee/uniquery/IndexApp/logs

# These two scripts can be run locally without ill consequences.
# STATICS = check_for_duplicates.pl count_pages.pl


# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules



# ---------------  Misc Rules  ------------------------------------

# Check crawled log for dups
checkdupscrawl :
	./check_for_duplicates.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/crawledUrls.log

# Check indexed log for dups
checkdupsindex :
	./check_for_duplicates.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/indexedUrls.log


# Count crawled pages
countcrawl :
	./count_pages.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/crawledUrls.log

# Count indexed pages
countindex :
	./count_pages.pl $(TARGETROOT)/j2ee/uniquery/IndexApp/logs/indexedUrls.log
