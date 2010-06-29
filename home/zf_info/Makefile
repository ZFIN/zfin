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

SUBDIRS = anatomy catch dbase images monitor movies news \
	zfbook sequence 2006_tutorial

GENERICS = zfprbs.html downloads.html stars.html

STATICS = 227.gif fish.gif logo.jpeg genome.html updates.html  citation.html \
	translocation.jpeg zfin.gif anatomy.html submissions.html zebrafish2009.pdf  \
	glossary.html GO.html oev.html nomen_comm.html blast_info.html new_gene.html \
	ontology_relationship_info.html author_guidelines.html author_guide_checklist.html \
	zfin_stats.html zfin_statistics.jpg syntax_help.html new_line.html nomen.html \
	contact_us.html nomen_tutorial.html defs.html


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules


