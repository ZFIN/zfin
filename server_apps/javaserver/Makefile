#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Java Server subdirectory
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
# This makefile uses a staging directory to produce most of its targets.
# This makefile populates the staging directory with source files and
# then calls the makefile in the staging directory to produce the 
# targets.  The staging makefile makes use of makefile variables 
# inherited from this file.
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/javaserver

STAGINGDIR = Staging


# ---- Scripts

SCRIPTGENERICS  = informix-javaserver runServer
SCRIPTSPECIFICS = $(foreach SGEN, $(SCRIPTGENERICS), $(TARGETDIR)/$(SGEN))

# The precc script is used to compile the javaserver.  It is not part of
# the production environment, so don't copy it to the target directory.
# precc does not have any generic tags in it.


# ---- Java source variables

JAVAGENERICS  = Server.java DBConnection.java
JAVASPECIFICS = $(foreach GNRC, $(JAVAGENERICS), $(STAGINGDIR)/$(GNRC))

JAVASTATICS  = MIConnection.java Statement.java ResultSet.java \
               SQLException.java 
JAVASTATICSTAGINGS = $(foreach STAT, $(JAVASTATICS), $(STAGINGDIR)/$(STAT))

JAVASRCS     = $(JAVAGENERICS) $(JAVASTATICS)
JAVASTAGINGS = $(JAVASPECIFICS) $(JAVASTATICSTAGINGS)



# ---- C source variables

# This makefile is concerned only with c source files that already exist.
# The staging directory is also concerned with C source files that are
# generated from the java class files.  All of the prexisting C source
# files need to be compiled with special MI options.

MICSRCS     = MIConnectionImp.c
MICSTAGINGS = $(foreach MIC, $(MICSRCS), $(STAGINGDIR)/$(MIC))



# ---- Which variables are used by Makefile in Staging directory:

export JAVASRCS
export MICSRCS
export TARGETDIR


# define target list

TARGETS = $(SCRIPTSPECIFICS) $(JAVASTAGINGS) $(MICSTAGINGS) 


# ---------------  Production Rules  ------------------------------------

.PHONY : all start stop sanitycheck clean clobber depend onetimeonly $(STAGINGDIR)


all : $(TARGETDIR) $(TARGETS) $(STAGINGDIR) 

$(TARGETDIR) :
	$(TARGET_MKDIR) $@

$(STAGINGDIR) :
	(cd $(STAGINGDIR); $(MAKE))



# ----  Rules for moving scripts to target directory

$(SCRIPTSPECIFICS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@



# ----  Rules for moving Java sources to staging directory.

$(JAVASPECIFICS) : $(STAGINGDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@

$(JAVASTATICSTAGINGS) : $(STAGINGDIR)/% : %
	$(STAGING_CP) $< $@


# ----  Rules for copying C files

$(MICSTAGINGS) : $(STAGINGDIR)/%.c : %.c
	$(STAGING_CP) $< $@



# ---------------  Start/Stop Rules  ------------------------------------

start : 
	$(TARGETDIR)/informix-javaserver start

stop : 
	$(TARGETDIR)/informix-javaserver stop



# ---------------  Maintenance Rules  -----------------------------------

clean :		# remove copied files from staging directory
	rm -rf $(JAVASTAGINGS) $(MICSTAGINGS)
	$(MAKE) -C $(STAGINGDIR) $@


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)
	$(MAKE) -C $(STAGINGDIR) $@


# check generics & possibly statics as well for values that should
# be represented with tags instead of hard coded values

sanitycheck : 
	$(SPECIFICCHECK) $(JAVASRCS) $(MICSRCS)
	$(MAKE) -C $(STAGINGDIR) $@


# Update header file dependencies in staging directory.

depend : 
	(cd $(STAGINGDIR); $(MAKE) depend)



# define one time only target

onetimeonly :
	$(ONETIMEONLY)
