#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Static web pages (zf_info)
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Thought about having this makefile make all of the static web pages in
# one fell swoop, without the use of recursive makefiles.  That approach
# would have used something like a smart copy that wouldn't copy CVS
# directories and ~ files, but would copy and makespecific everything else.
#
# The problem with that approach is that it would copy test files, alternate
# versions of pages and any other flotsam that was in the directories.  This 
# would tend to pollute the production directories over time.  It also doesn't
# deal with the SEARCH_SITE directory very well.
#
# So instead we have the rather tedious job of maintaining a nearly identical
# makefile in each directory.  However, this will tend to keep the production
# directory clean.
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/javascript

SUBDIRS  = ajax-lib

STATICS = patocuration.js ncbo-term-info.js term_completer.js \
	dichty-term-info.js json.js sessionstate.js prototype.js \
	awstats_misc_tracker.js control.modal.js control.modal.2.2.3.js

GENERICS = header.js footer.js imagebox.js


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules


