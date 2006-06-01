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

TARGETDIR = $(TARGETROOT)/home/zf_info

SUBDIRS = SEARCH_SITE anatomy catch dbase images monitor movies news stckctr \
	zfbook sequence

GENERICS = defs.html genome.html nomen.html nomen_comm.html \
	updates.html zfprbs.html citation.html anatomy.html contact_us.html \
	glossary.html GO.html oev.html downloads.html nomen_tutorial.html \
	stars.html blast_info.html

STATICS = 227.gif fish.gif logo.jpeg \
	translocation.jpeg zfin.gif 



# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules


