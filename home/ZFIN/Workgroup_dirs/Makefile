#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW/home/ZFIN/Workgroup_dirs 
# subdirectory
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
# This directory contains only one entry, a symbolic link to the FTP
# directory on chromix.  Originally, the web pages pointed to the FTP
# directory (via the sym link here) for several pages on anatomical
# dictionaries.  However, those files have now been duplicated and
# reside in zf_info/anatomy/Dictionary.
#
# The sym link (and this directory) remain, however, in order to support
# web pages that support the Anatomical dictionary work group.



# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/ZFIN/Workgroup_dirs



# ---------------  Variable Definitions  --------------------------------


# This has one target in the target dir, a symbolic link.

ANATDICT_FTP_TARGET = $(TARGETDIR)/AnatDict

TARGETS = $(ANATDICT_FTP_TARGET)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(ANATDICT_FTP_TARGET) : 
	ln -s /research/zfin/chromix/ftp/pub/AnatDict $(ANATDICT_FTP_TARGET)



# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)


sanitycheck :


# define one time only target

onetimeonly :
	$(ONETIMEONLY)
