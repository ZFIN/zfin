#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Linking Authors to Publications
# Applet.
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

IMPORTPATH = $(JDBCHOME)/lib
IMPORTJAR = ifxjdbc.jar

# ---- Java source variables

GENERICS = SQLQuery.java
STATICS  = ListItem.java ListSelector.java Selectable.java \
	SelectedPanel.java SystemsPanel.java SelectablePanel.java


SPECIFICS = $(foreach GNRC, $(GENERICS), $(STAGINGDIR)/$(GNRC))
STATICSTAGINGS = $(foreach STAT, $(STATICS), $(STAGINGDIR)/$(STAT))

JAVAS   = $(GENERICS) $(STATICS)
TARGETS = $(SPECIFICS) $(STATICSTAGINGS)

# ---- Java destination variables

JAR = listselector.jar

# ---- Which variables are used by Makefile in Staging directory:

export JAVAS JAR IMPORTPATH IMPORTJAR



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly $(STAGINGDIR)


all : $(TARGETS) $(STAGINGDIR) 

$(STAGINGDIR) :
	(cd $(STAGINGDIR); $(MAKE))


# ----  Rules for moving Java sources to staging directory.

$(SPECIFICS) : $(STAGINGDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@

$(STATICSTAGINGS) : $(STAGINGDIR)/% : %
	$(STAGING_CP) $< $@
	$(TOUCH) $@


# ---------------  Maintenance Rules  -----------------------------------

clean :		# remove copied files from staging directory
	rm -rf $(JAVASTAGINGS)
	$(MAKE) -C $(STAGINGDIR) $@


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)
	$(MAKE) -C $(STAGINGDIR) $@


# check generics & possibly statics as well for values that should
# be represented with tags instead of hard coded values

sanitycheck : 
	$(SPECIFICCHECK) $(JAVAS)
	$(MAKE) -C $(STAGINGDIR) $@


# define one time only target

onetimeonly :
	$(ONETIMEONLY)
