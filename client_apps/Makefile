#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  Client Apps (applets) directory
#
# $Source$
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

TARGETDIR = $(TARGETROOT)/client_apps

# do not include gbrowse/ as a SUBDIR
# it will force it to be built before it's Makefile is generated
SUBDIRS = Map

# this means gmake must be run in /lib/Perl before here.
PERL = /private/bin/perl10 -I $(TARGETROOT)/lib/Perl/bioperl -I $(TARGETROOT)/lib/Perl/gbrowse

# the list of gbrowse files we edit  
GBROWSE  = gbrowse/Makefile.PL \
	gbrowse/cgi-bin/gbrowse.PLS \
	gbrowse/cgi-bin/gbrowse_img.PLS \
	gbrowse/lib/Bio/DB/SeqFeature/Store/DBI/ifx.pm \
	gbrowse/conf/*.conf
	 # ../lib/Perl/bioperl/Bio/

all:  
	
# without DO_XS=1 creates compiled .pmc files for x86
# $(TARGETROOT)/lib/Perl/gbrowse/Bio/ needs to exist before first run 
# so hacking it in everytime
gbrowse: $(GBROWSE)
	mkdir -p $(TARGETROOT)/lib/Perl/gbrowse/Bio
	rsync -r --delete -C --exclude=.svn ./gbrowse/lib/Bio/ $(TARGETROOT)/lib/Perl/gbrowse/Bio/
	-cd gbrowse && $(PERL) Makefile.PL  \
		     APACHE=$(TARGETROOT)/home \
		     LIB=$(TARGETROOT)/lib/Perl/gbrowse \
		     BIN=$(TARGETROOT)/bin \
		     PREFIX=$(TARGETROOT) \
		     HTDOCS=$(TARGETROOT)/home \
		     GBROWSE_ROOT=gbrowse \
		     CGIBIN=$(TARGETROOT)/cgi-bin \
			 CONF=$(TARGETROOT)/home/conf \
		     INSTALLSITEMAN1DIR=0 \
		     INSTALLSITEMAN3DIR=0 \
	         INFORMIXDIR=$(INFORMIXDIR)  \
	         INFORMIXSERVER=$(INFORMIXSERVER) \
	         SQLHOSTS_FILE=$(INFORMIXSQLHOSTS) \
			 NONROOT=1 \
			 DO_XS=0

	-cd gbrowse && gmake
	-cd gbrowse && gmake install



# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules
