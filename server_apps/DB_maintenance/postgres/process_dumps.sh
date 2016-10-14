#!/bin/bash

unloadDirectory=$1

if [[ -d $1 ]]; then
    echo "$1 is a directory"
   
else
    echo "$1 is not valid"
    exit 1
fi

cd $unloadDirectory

rm unload_*
rm *.err
rm *.out
rm btsfse*
rm done
rm environment_staging
rm fish_staging
#not sure why these two didn't come thru in the schema file
rm paneled_markers
rm pub_db_xref
#remove more datablade bits
#also had to remove most recent schema changes: TODO: apply liquibase changes to postgres schema as well.
rm staging_webpages
rm sysblderrorlog
rm sysbldiprovided
rm sysbldirequired
rm sysbldobjdepends
rm sysbldobjects
rm sysbldobjkinds
rm sysbldregistered
rm syserrors
rm systraceclasses
rm systracemsgs
rm webcmimages
rm webcmpages
rm webconfigs
rm webenvvariables
rm webpages
rm webtags
rm webudrs

data_files=`ls *`

ls --hide="*.*" > filenames.txt

for f in $data_files
do
    echo $f
    sed 's/|$//' $f > $f.t
# data_note needs not to have \r replaced.
#external_note had to hand edit file to remove ^M line 1331 then look for ^M
  #  sed 's/\r/\\r/g' $f.t > $f.txt
    sed 's/\r/\n/g' $f.t > $f.txt
    #this is just for figure, lab, lab_address_update_tracking, publication, updates so far...
    #sed 's/\r//g' $f.txt > temp
    mv temp $f.txt
    rm f.t
done


