#!/bin/sh
#
# Convert VegaProtein fa files to VegaProtein wublast files
#
# Input: 
#      -v            verbose
#      fasta file
 
echo "==| at VegaProtein |=="

rm -f xdformat_vegaprotein_zf.log

echo "==| Format the file into blast db |== "

xdformat -p -e xdformat_vegaprotein_zf.log -t "Zebrafish VegaProtein Transcripts" -I -o vegaprotein_zf vegaprotein_zf.fa


echo "==| Exit convertVegaProtein.sh |== "

exit
