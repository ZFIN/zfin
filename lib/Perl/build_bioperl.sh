#!/bin/sh

mkdir -p $TARGETROOT/lib
rsync -r --delete -C --exclude=Makefile ./bioperl $TARGETROOT/lib/Perl && cp -fr  ./cpan/Bio/* $TARGETROOT/lib/Perl/bioperl/Bio/

