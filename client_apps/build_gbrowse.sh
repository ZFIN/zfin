#!/bin/sh

cd gbrowse

mkdir -p $TARGETROOT/tmp
chmod o+w $TARGETROOT/tmp
mkdir -p $TARGETROOT/lib/Perl/gbrowse/Bio

rsync -r --delete -C --exclude=.svn ./lib/Bio/ $TARGETROOT/lib/Perl/gbrowse/Bio/

#/private/bin/perl
/usr/bin/perl -I $TARGETROOT/lib/Perl/bioperl -I $TARGETROOT/lib/Perl/gbrowse Makefile.PL APACHE=$TARGETROOT LIB=$TARGETROOT/lib/Perl/gbrowse BIN=$TARGETROOT/bin PREFIX=$TARGETROOT HTDOCS=$TARGETROOT GBROWSE_ROOT=gbrowse CGIBIN=$TARGETROOT/cgi-perl CONF=$TARGETROOT/conf INSTALLSITEMAN1DIR=0 INSTALLSITEMAN3DIR=0 INFORMIXDIR=$INFORMIXDIR INFORMIXSERVER=$INFORMIXSERVER SQLHOSTS_FILE=$INFORMIXSQLHOSTS NONROOT=1 DO_XS=0

#cd gbrowse && 
make
#cd gbrowse && 
make install
