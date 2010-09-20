#!/bin/sh

for f in `ls *.env | cut -d'.' -f1`; do
cp $f.env $f.properties
done

