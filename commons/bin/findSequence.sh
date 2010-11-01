#!/bin/bash

if [ $# != 3 ]; then
  echo "Usage: findSequence.sh <directory> <accession> <p|n>"
  echo "Example: ./findSequence.sh  /nfs/zygotix/zblastfiles/zmore/default_r11/Current NP_571379 p"
  exit ;
fi

# $Id: $
for i in `ls $1/*.x$3i | cut -f1 -d.`; do
  echo $i
  echo "xdget -$3 $i $2 2>&1 " ;
  xdget -$3 $i $2 2>&1 | grep -vi "Not found" ;
done


