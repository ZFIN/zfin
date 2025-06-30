#!/bin/tcsh
#
# Usage:
#       loadGBdiv.sh  div
# Input:
#       div   genbank division
#
# download one genbank division
# Output: 
#        current dir
#

if ($#argv != 0 ) then
    set prefix = $argv[1]
    
    echo "download gb$prefix*.seq.gz"
    date
    /local/bin/wget -q -r -l 2 -A "gb$prefix*.seq.gz" ftp://ftp.ncbi.nih.gov/genbank/ 
    date

    mkdir $prefix
    echo "mv to $prefix dir"
    mv ftp.ncbi.nih.gov/genbank/gb"$prefix"*.seq.gz $prefix
else
    echo "please provide the catagory name e.g. pri, est"
endif
