#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Genomic Map Viewer
# Applet.
#
# $Source$

#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile uses a staging directory to produce its targets.
# This makefile populates the staging directory with source files and
# then calls the makefile in the staging directory to produce the 
# targets.  The staging makefile makes use of makefile variables 
# inherited from this file.
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

# TARGETDIR is handled by Staging directory

STAGINGDIR = Staging
STAGINGZMAPPER = $(STAGINGDIR)/zmapper
IMAGES = mapkey.gif

# ---- Java source variables

GENERICS = zmapper/SQLQuery.java
STATICS  = mapplet.java mapimage.java cgi_lib.java \
	zmapper/BackBone.java zmapper/Bin.java zmapper/BinGroup.java \
	zmapper/LayoutManager.java zmapper/MapViewer.java zmapper/Marker.java \
	zmapper/MarkerBuilder.java 

SPECIFICS = $(foreach GNRC, $(GENERICS), $(STAGINGDIR)/$(GNRC))
STATICSTAGINGS = $(foreach STAT, $(STATICS), $(STAGINGDIR)/$(STAT))

JAVAS   = $(GENERICS) $(STATICS)
TARGETS = $(SPECIFICS) $(STATICSTAGINGS)

# ---- Java destination variables

JAR = mapplet-1.0.jar

# ---- Which variables are used by Makefile in Staging directory:

export JAVAS JAR IMAGES



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly $(STAGINGDIR)


all : $(STAGINGZMAPPER) $(TARGETS) $(STAGINGDIR) 

$(STAGINGZMAPPER) :
	$(TARGET_MKDIR) $@

$(STAGINGDIR) :
	(cd $(STAGINGDIR); $(MAKE))


# ----  Rules for moving Java sources to staging directory.

$(SPECIFICS) : $(STAGINGDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@

$(STATICSTAGINGS) : $(STAGINGDIR)/% : %
	$(STAGING_CP) $< $@



# ---------------  Maintenance Rules  -----------------------------------

clean :		# remove copied files from staging directory
	rm -rf $(JAVASTAGINGS) $(STAGINGZMAPPER)
	$(MAKE) -C $(STAGINGDIR) $@


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS) $(STAGINGZMAPPER)


# check generics & possibly statics as well for values that should
# be represented with tags instead of hard coded values

sanitycheck : 
	$(SPECIFICCHECK) $(JAVAS)
	$(MAKE) -C $(STAGINGDIR) $@


# define one time only target

onetimeonly :
	$(ONETIMEONLY)
