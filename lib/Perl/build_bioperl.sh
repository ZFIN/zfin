#!/bin/sh

rsync -r -C --exclude=Makefile ./bioperl/* $LIBTARGET && cp -fr  ./cpan/Bio/* $LIBTARGET/Bio/


