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

PROG=cgiemail cgiecho cgifile cgicso

DISTFILES=README ChangeLog *.[ch] configure *.in testce.txt
DISTBASE=cgiemail-$(RELEASE)
DISTTAR=$(DISTBASE).tar
DISTDIR=/usr/tmp
DISTHOME=/mit/wwwdev/cgiemail

ECHO_OBJ=cgilib.o cgiecho.o
EMAIL_OBJ=cgilib.o cgiemail.o
FILE_OBJ=cgilib.o cgifile.o
CSO_OBJ=cgilib.o cgilibcso.o csoget.o cgicso.o
CSODIR=/afs/net.mit.edu/admin/www/root
CSODEFS=-DCSOHEADER=\"$(CSODIR)/csoheader\" -DCSOFOOTER=\"$(CSODIR)/csofooter\"
# For CodeCenter
CSODEFS2=-DCSOHEADER="$(CSODIR)/csoheader" -DCSOFOOTER="$(CSODIR)/csofooter"
LIBSRC=cgilib.c cgilibcso.c

all: $(PROG)
	cp -p cgiemail $(TARGETDIR)

cgiecho: $(ECHO_OBJ)
	$(CC) $(LDFLAGS) -o $@ $(ECHO_OBJ)

cgiemail: $(EMAIL_OBJ)
	$(CC) $(LDFLAGS) -o $@ $(EMAIL_OBJ)

cgifile: $(FILE_OBJ)
	$(CC) $(LDFLAGS) -o $@ $(FILE_OBJ)

cgilibcso.o: cgilibcso.c Makefile
	$(CC) $(CFLAGS) $(CSODEFS) -c $*.c

cgicso: $(CSO_OBJ)
	$(CC) $(LDFLAGS) -o $@ $(CSO_OBJ) -lsocket -lnsl 

check:
	@echo No tests are supplied.



register: all
	(echo To: cgiemail@mit.edu; echo Subject: built cgiemail on `uname`; \
	 echo ''; uname -a) | /usr/lib/sendmail -t

distclean: clean
	rm -f Makefile config.h config.status config.cache config.log

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

# Autoconf rules

Makefile config.h: Makefile.in config.status
	./config.status

config.status: configure
	./config.status --recheck

configure: configure.in
	autoconf

# CodeCenter rules

cgiemail_src:
	#load cgilib.c cgiemail.c
	#setenv QUERY_STRING email=brlewis@mit.edu\&required-subject=subj\&body=test\&
	#setenv REQUEST_METHOD GET
	#setenv PATH_TRANSLATED /mit/brlewis/www/comments.txt

cgicso_src:
	#load $(CSODEFS2) cgilib.c cgilibcso.c csoget.c cgicso.c -lsocket -lnsl 
	#setenv QUERY_STRING query=logan
	#setenv REQUEST_METHOD GET
	#setenv SCRIPT_NAME /cgi-bin/cgicso

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
