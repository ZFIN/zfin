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

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/$(TARGETCGIBIN)
TARGETS = $(TARGETDIR)/cgiemail

################################################################################

# Generated automatically from Makefile.in by configure.
# Makefile.in - Used by configure to generate Makefile
SHELL=/bin/sh

RELEASE=1.6
CC=gcc

PROG=cgiemail

DISTFILES=README 
DISTBASE=cgiemail-$(RELEASE)
DISTDIR=/usr/tmp

EMAIL_OBJ=cgilib.o cgiemail.o

LIBSRC=cgilib.c cgilibcso.c

all: $(PROG)
	cp -pf cgiemail $(TARGETDIR)
	chmod 555 $(TARGETS)

cgiemail: $(EMAIL_OBJ)
	$(CC) $(LDFLAGS) -o $@ $(EMAIL_OBJ)


dist:
	rm -rf $(DISTDIR)/$(DISTBASE)
	mkdir -p $(DISTDIR)/$(DISTBASE)
	cp -pr $(DISTFILES) $(DISTDIR)/$(DISTBASE)
	(cd $(DISTDIR); tar cfh $(DISTTAR) $(DISTBASE))
	cp $(DISTDIR)/$(DISTTAR) $(DISTHOME)
	gzip $(DISTDIR)/$(DISTTAR)
	cp $(DISTDIR)/$(DISTTAR).gz $(DISTHOME)

cgi-ptypes.h: $(LIBSRC)
	mkptypes $(LIBSRC) > $@


version.ht: all
	sysname=`fs sysname | awk -F\' '{ print $$2 }'`; \
	echo '<tr><td><a href="../arch/'$$sysname'/cgi-bin">SunOS 5.8</a></td><td><code>/usr/lib/sendmail</code></td> <td><a href="#$(RELEASE)">$(RELEASE)</a></td></tr>' > $@

# ---------------  Maintenance Rules  -----------------------------------
clean:
	rm -f $(PROG) *.o core $(TARGETS)
  
clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)

sanitycheck :

# define one time only target for special uses

onetimeonly :
	$(ONETIMEONLY)
