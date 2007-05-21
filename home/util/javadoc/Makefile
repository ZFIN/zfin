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

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home

# Note that from an http point of view there are several other subdirectories
# of this directory.  However they are not stored as subdirectories in the
# file system.

SUBDIRS  =   
 
POSTLOADDB_SUBDIRS = 

GENERICS = 

STATICS  =  


# ---------------  Production Rules  ------------------------------------

# Bring in standard set of make rules

include $(TOP)/make.default.rules


# Rules that are endemic to this makefile



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


run : 
	 $(foreach DIR,$(SUBDIRS), $(MAKE) -C $(DIR) $@)


postloaddb :
	$(foreach PLDIR,$(POSTLOADDB_SUBDIRS), $(MAKE) -C $(PLDIR) $@)
