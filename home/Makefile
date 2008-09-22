#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW/home subdirectory
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

SUBDIRS  = ZFIN zf_info javascript css images unavailable util
MIRROR_SUBDIRS = zf_info images
POSTLOADDB_SUBDIRS = ZFIN

GENERICS = robots.txt redirect.html \
	warranty.html zfin_is_down.html index.asis 

STATICS  = favicon.ico 

# This also creates a symbolic link to to an FTP directory to provide an 
# http interface to the FTP directory.

FTPTARGET = $(TARGETDIR)/transfer

# It also creates a sym link to the client_apps directory.  This is to
# overcome a bug in apache.

CLIENTAPPSTARGET = $(TARGETDIR)/client_apps

GENERATE_VERSION : gmake-version.html
	

ENDEMICTARGETS_POSTTARGETS = $(FTPTARGET) $(CLIENTAPPSTARGET) $(GENERATE_VERSION) 

# If we are building a mirror then disable calling gmake with no target.

ifeq ($(INFORMIXSERVER),bogus.informix.server.for.mirrors)
InvokingMirrorWithoutTarget:
	ERROR: Reinvoke gmake with a mirror target.
endif


# ---------------  Production Rules  ------------------------------------

# Bring in standard set of make rules

include $(TOP)/make.default.rules


# Rules that are endemic to this makefile

$(FTPTARGET) : 
	if [ ! -h $(FTPTARGET) ] ; then ln -s $(TARGETFTPROOT)/pub/transfer $(FTPTARGET); fi


$(CLIENTAPPSTARGET) :
	if [ ! -h $(CLIENTAPPSTARGET) ] ; then ln -s $(TARGETDIR)/../client_apps $(CLIENTAPPSTARGET); fi

gmake-version.html :
	cd .. ; ./buildfiles/generateVersion.sh $(TARGETROOT)/home/gmake-deploy-version.html  ; cd home


# ---------------  Special Targets  ------------------------------------

# Special target to create only the directories and files used in mirror sites.
# In this case, that's all the files in this directory, plus a subset of the
# subdirectories.  Note that this is the lowest level "mirror" target in the 
# hierarchy.  Below here, the regular targets are used.
#
# After all the files have been made, replace the local index.html with 
# a modified copy of ZDB_home.apg.  The original index.html redirects 
# to ZDB_home.apg, which is the frameless ZFIN home page.  However, mirrors 
# can't use that directly as it requires an underlying database, and mirrors
# don't have that.
#
# Therefore we strip all of the MI tags from the ZDB_home.apg and make it
# into index.html for the mirror sites.

mirror : $(TARGETDIR) $(MIRROR_SUBDIRS) $(SPECIFICTARGETS) $(STATICTARGETS)
	stripmicode.pl < ZFIN/APP_PAGES/misc_files/ZDB_home.apg > mirror_index.html
	chmod 644 mirror_index.html
	$(MAKESPECIFIC) mirror_index.html $(TRANSLATETABLE) $(TARGETDIR)/index.html
	rm -f mirror_index.html


postloaddb :
	$(foreach PLDIR,$(POSTLOADDB_SUBDIRS), $(MAKE) -C $(PLDIR) $@)
