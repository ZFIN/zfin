#!/bin/bash
TEMPFILE=./fasta1
touch $TEMPFILE
echo "xdformat -$1 -I -o $2 $TEMPFILE"
xdformat -$1 -I -o $2 $TEMPFILE
echo "mv $2.* $3"
mv $2.* $3
rm -f $TEMPFILE


