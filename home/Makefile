#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW/home subdirectory
#
# $Source$
# $Id$
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home

# Note that from an http point of view there are several other subdirectories
# of this directory.  However they are not stored as subdirectories in the
# file system.

SUBDIRS  = ZFIN zf_info images
MIRROR_SUBDIRS = zf_info images

GENERICS = index.html robots.txt

STATICS  = fish_bgd.gif fish_net.gif

# This also creates a symbolic link to to an ftp directory to provide an 
# http interface to the FTP directory.  This is currently hardcoded to
# point at chromix.

FTPTARGET = $(TARGETDIR)/transfer

SPECIFICTARGETS = $(foreach SPEC, $(GENERICS), $(TARGETDIR)/$(SPEC))
STATICTARGETS   = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
TARGETS         = $(SPECIFICTARGETS) $(STATICTARGETS) $(FTPTARGET)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber $(SUBDIRS) mirror


all : $(TARGETDIR) $(TARGETS) $(SUBDIRS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(SPECIFICTARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@

$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@

$(FTPTARGET) : 
	ln -s /research/zfin/chromix/ftp/pub/transfer $(FTPTARGET)

$(SUBDIRS) :
	$(MAKE) -C $@


# ---------------  Special Targets  ------------------------------------

# Special target to create only the directories and files used in mirror sites.
# In this case, that's all the files in this directory, plus a subset of the
# subdirectories.  Note that this is the last "mirror" target in the hierarchy.
# Below here, the regular targets are used.
# After everuything to mirror has been mirrored, create a tar file containing
# all of them.

mirror : $(MIRROR_SUBDIRS) $(SPECIFICTARGETS) $(STATICTARGETS)
	(cd $(TARGETDIR); tar cvfp $(TARGETROOT)/mirror.tar $^)
	gzip -f $(TARGETROOT)/mirror.tar


# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)


sanitycheck :
	$(SPECIFICCHECK) $(GENERICS)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# define one time only target to make the specific files that were intially
# checked in to CVS, make them generic, and then check them back in to CVS.

cvsgeneric :
	$(foreach GNRC,$(GENERICS), $(CVSGENERIC))
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )
