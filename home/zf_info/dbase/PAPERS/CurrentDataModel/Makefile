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
# All this makefile does is create symbolic links into the ZFIN Commons
# directory.  Note that it makes links into the ZFIN Commons directory
# on bionix, rather than to the backup copy on chromix.  This means that
# if bionix is down then these documents won't be available.  On the other
# hand, when bionix is up, the latest and greatest copies of these documents
# will be available.
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/zf_info/dbase/PAPERS/CurrentDataModel
DOCDIR = /research/zfin/central/Commons/doc

# All of the local files are sym links that point at internal.html.

INTERNALFILE = internal.html
MISSINGLINKS = admin.html curation.html impl.html index.html

INTERNALFILETARGET = $(TARGETDIR)/$(INTERNALFILE)
MISSINGLINKTARGETS = $(foreach LINK, $(MISSINGLINKS), $(TARGETDIR)/$(LINK))

REMOTEFILES = db.html Database
REMOTELINKS = $(foreach FILE, $(REMOTEFILES), $(TARGETDIR)/$(FILE))

TARGETS     = $(INTERNALFILETARGET) $(MISSINGLINKTARGETS) $(REMOTELINKS)


# ---------------  Production Rules  ------------------------------------

# Make 4 things:
# 1. The target directory
# 2. internal.html
# 3. sym links from the missing files to internal.html
# 4. sym links to the real db.html and database directories.

all : $(TARGETDIR) $(TARGETS)

$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

# The touch is needed because of timestamp problems.
$(INTERNALFILETARGET) : $(INTERNALFILE)
	$(TARGET_CP) $^ $@
	touch $@

$(MISSINGLINKTARGETS) : $(INTERNALFILETARGET)
	ln -s $^ $@

$(REMOTELINKS) : $(TARGETDIR)/% : $(DOCDIR)/%
	ln -s $< $@

# ---------------  Maintenance Rules  -----------------------------------

clean :			 # No local intermediate files
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )

clobber :		       # Remove targets, but not target dir.
  ifdef TARGETS
	rm -f $(TARGETS)
  endif
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )

sanitycheck :
    ifdef GENERICS
	$(SPECIFICCHECK) $(GENERICS)
    endif
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# define one time only target for special uses

onetimeonly :
	$(ONETIMEONLY)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )
