#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Current ZFIN Data Model Description.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile creates symbolic links into the ZFIN Commons Doc directory 
# so that the database documentation can be available to the public
# through the web site.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/zf_info/dbase/PAPERS/CurrentDataModel
DOCDIR = /private/ZfinLinks/Commons/doc

# All of the local files are sym links that point at internal.html.

INTERNALFILE = internal.html
MISSINGLINKS = admin.html curation.html impl.html index.html intro.html \
               standards.html

INTERNALFILETARGET = $(TARGETDIR)/$(INTERNALFILE)
MISSINGLINKTARGETS = $(foreach LINK, $(MISSINGLINKS), $(TARGETDIR)/$(LINK))

REMOTEFILES = db.html Database
REMOTELINKS = $(foreach FILE, $(REMOTEFILES), $(TARGETDIR)/$(FILE))

ENDEMICTARGETS_POSTTARGETDIR = $(INTERNALFILETARGET) $(MISSINGLINKTARGETS) $(REMOTELINKS)


# ---------------  Production Rules  ------------------------------------

# Make 4 things:
# 1. The target directory
# 2. internal.html
# 3. sym links from the missing files to internal.html
# 4. sym links to the real db.html and database directories.

# use the default set of rules

include $(TOP)/make.default.rules

# The touch is needed because of timestamp problems.
$(INTERNALFILETARGET) : $(INTERNALFILE)
	$(TARGET_CP) $^ $@
	$(TOUCH) $@

$(MISSINGLINKTARGETS) : $(INTERNALFILETARGET)
	ln -s $^ $@

$(REMOTELINKS) : $(TARGETDIR)/% : $(DOCDIR)/%
	/bin/rm -f $@
	ln -s $< $@

