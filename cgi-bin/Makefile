#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, ZFIN CGI binary directory.
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

TARGETDIR = $(TARGETROOT)/$(TARGETCGIBIN)


# The webdrivers in this directory need an explanation. 
#
# First, why are there any webdriver executables in this directory at all?
# Starting in late 2000 / early 2001 we started using the webdriver that
# is an apache module.  That version of the webdriver is kept elsewhere,
# and is distinct from the old standalone webdriver that this directory
# holds.  Prior to going to the apache-integrated version of webdriver, we
# used the standalone webdriver that exists in this directory for all 
# app page accesses.  We made the switch because the apache-integrated
# version is much, much faster.
#
# In the new setup, the apache aware webdriver is called only for webdriver 
# calls that come in directly through HTTP.  However, those are not the only
# calls to webdriver that our site makes.  We have several CGI scripts that 
# call the webdriver executable directly.  
#
# Also, most (all?) of our update pages call /cgi-bin/SuperWebdriver, 
# instead of /cgi-bin/webdriver.  SuperWebdriver then invokes Webdriver,
# which is currently just a sym link to webdriver.
#
# In order to deal with the scripts that call webdriver directly (i.e. not
# through HTTP) we need to keep a webdriver executable and web.cnf file
# in the cgi-bin directory.  
# 
# It may be possible/advisable to change the scripts to call webdriver 
# through HTTP, rather than directly, but that is past my current HTTP 
# skill level.
#
# So here's the summary on the different webdrivers in this directory.
#
#  webdriver	      Old style stand alone webdriver executable.  Called
#			only by scripts in the cgi-bin directory.
#  Webdriver	      Currently a sym link to webdriver.  Used only by
#			SuperWebdriver
#  SuperWebdriver     A CGI script called by many (possibly all) update
#			app pages.  This is the only script that uses
#			the Webdriver sym link.
#  web.cnf	      Webdriver configuration file used by the local 
#			webdriver executable.
#


STATICS = dummy.gif webdriver write.cgi parse-form.perl urlencode.perl

# send_request.perl can probably be converted to static.

GENERICS = SuperWebdriver ZFIN_jump map-options.pl print_map.cgi \
           runSPL.cgi DebugWebdriver\
           searchwww.cgi web.cnf send_request.perl view_mapplet.cgi

SPECIFICTARGETS = $(foreach SPEC, $(GENERICS), $(TARGETDIR)/$(SPEC))
STATICTARGETS   = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))

TARGETS = $(SPECIFICTARGETS) $(STATICTARGETS) $(TARGETDIR)/Webdriver



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS) 

# Create target dir and then file-uploads directory.  It is used by
# SuperWebdriver to store image files before they are loaded into the DB. 

$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR)
	$(TARGET_MKDIR) $(TARGETDIR)/file-uploads

$(SPECIFICTARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@


$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@

$(TARGETDIR)/Webdriver : $(TARGETDIR)/webdriver
	(cd $(TARGETDIR); ln -s webdriver Webdriver)



# ---------------  Maintenance Rules  -----------------------------------

clean :		# No intermediate files to get rid of.


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)


sanitycheck :
	$(SPECIFICCHECK) $(STATICS) $(GENERICS)


# define one time only target 

onetimeonly :
	$(ONETIMEONLY)
