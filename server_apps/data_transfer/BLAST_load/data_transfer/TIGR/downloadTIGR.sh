#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR

# read in--  ftp url| path | file name |
# to get file name, and increase it by one for future process

set ftpdomain = `cut -d\| -f1 @SCRIPT_PATH@/TIGR/tigr.ftp`
set ftppath = `cut -d\| -f2 @SCRIPT_PATH@/TIGR/tigr.ftp`
set filename = `cut -d\| -f3 @SCRIPT_PATH@/TIGR/tigr.ftp`

echo "==| Remove old file from TIGR dir|== "
rm -f ZGI.*
rm -f README*

# increase the version
set release = `echo $filename | cut -d. -f2`
set version = `echo $release | cut -d_ -f2`
@ nextversion = $version  + 1

set fnameroot = $filename:r
set fnamepre  = $fnameroot:r
set newfile = $fnamepre."release_"$nextversion.zip

echo "==| Download TIGR file ftp://$ftpdomain/$ftppath/$newfile |=="
wget -q ftp://$ftpdomain/$ftppath/$newfile

if (-e $newfile)  then
    echo 'new file exists $newfile';
else
    echo "release $nextversion does not exist."
    @ nextversion = $version ;
    set newfile = $fnamepre."release_"$nextversion.zip
    echo "==| Download TIGR file ftp://$ftpdomain/$ftppath/$newfile |=="
    wget -q ftp://$ftpdomain/$ftppath/$newfile
endif

echo "==| have to rm the tigr.ftp for TIGR and replace it with a new version |=="

rm -f @SCRIPT_PATH@/TIGR/tigr.ftp
echo $ftpdomain"|"$ftppath"|"$newfile"|" > @SCRIPT_PATH@/TIGR/tigr.ftp

echo "==| Unzip TIGR |== "

unzip $newfile

rm -f ZGI.release*


echo "==| Done with TIGR download |=="

exit
