#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, App Pages hierarchy.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#


# ---------------  Variable Definitions  --------------------------------


TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/ZFIN/APP_PAGES

SUBDIRS = \
	ZFINadmn \
	company_files \
	accession \
	fish_files \
	gene_files \
	image_files \
	lab_files \
	linkage \
	locus_files \
	marker_files \
	misc_files \
	navigation_files \
	personbrowser \
	pubbrowser \
	security_files \
	selection \
	updating \
	xpat_files \
        feature_files \
        curation_files



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber postloaddb $(SUBDIRS)


all : $(TARGETDIR) $(SUBDIRS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(SUBDIRS) :
	$(MAKE) -C $@



# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# The clobber target is why this file can't use the make.defualt.rules file.
# In addition to the usual chores of clobber.  This one also deletes all
# the app pages in the webpages table.

clobber :			# Remove targets, but not target dir.
	echo 'delete from webpages' | $(DBACCESS) $(DBNAME) 
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )

sanitycheck :
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


onetimeonly :
	$(ONETIMEONLY)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# ---------------  Special Targets  ------------------------------------

# The postloaddb target is invoked after loading a database to clean out and
# repopulate, among other things, the webpages table.  The postloaddb target
# here takes care of the webpages table.

postloaddb : clobber all

