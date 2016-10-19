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

SUBDIRS  = ZFIN zf_info unavailable util
MIRROR_SUBDIRS = zf_info images
POSTLOADDB_SUBDIRS = ZFIN

GENERICS =

STATICS  = favicon.ico warranty.html zfin_is_down.html robots.txt sitemap.xml

# This also creates a symbolic link to to an FTP directory to provide an
# http interface to the FTP directory.

FTPTARGET = $(TARGETDIR)/transfer

# It also creates a sym link to the client_apps directory.  This is to
# overcome a bug in apache.

CLIENTAPPSTARGET = $(TARGETDIR)/client_apps

GENERATE_VERSION = gmake-deploy-version.html

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

$(GENERATE_VERSION) :
	../buildfiles/generateVersion.sh $(TARGETROOT)/home/$(GENERATE_VERSION)


postloaddb :
	$(foreach PLDIR,$(POSTLOADDB_SUBDIRS), $(MAKE) -C $(PLDIR) $@)
