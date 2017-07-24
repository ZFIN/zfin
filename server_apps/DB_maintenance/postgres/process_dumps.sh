#!/bin/bash

startTime=$(date)
echo $startTime
echo ${DBNAME}

cd /research/zunloads/databases/trunkdb/

latestDump=`ls -td -- */ | head -n 1 | cut -d'/' -f1`
echo $latestDump


echo "*** Removing previous working directories"
dumpToRemove=/research/zunloads/databases/postgres_dumps/trunkdb/$latestDump;

echo "*** Dump to remove"
echo $dumpToRemove

rm -rf $dumpToRemove;

echo "removed"  

dirToCopy=/research/zunloads/databases/trunkdb/$latestDump;
echo "*** Directory to cp:"
echo $dirToCopy

destination=/research/zunloads/databases/postgres_dumps/${DBNAME};
echo "*** Destination to cp:"

echo $destination

cp -R $dirToCopy $destination
echo "copied"

echo "*** latest db specific dump to process: "
unloadDirectory=/research/zunloads/databases/postgres_dumps/${DBNAME}/$latestDump

echo $unloadDirectory

cd $unloadDirectory

echo "in current directory:"
pwd

rm unload_*
rm *.err
rm *.out
rm btsfse*
rm done
#not sure why these two didn't come thru in the schema file
#rm paneled_markers
#rm pub_db_xref
#remove more datablade bits
#also had to remove most recent schema changes: TODO: apply liquibase changes to postgres schema as well.
rm affected_gene_group
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

echo "*** save off blobs/clobs so they don't have to be processed for special characters."

ls --hide="*.*" > filenames.txt

data_files=`ls *`

echo "*** for each file, weed out characters: "

for loadFile in $data_files
do
    echo $loadFile
    
    #remove pipe at the end of the file b/c postgres thinks end of line pile is a delimiter 
    sed 's/|$//' $loadFile > $loadFile.t

    # data_note needs not to have \r replaced.
    # external_note had to hand edit file to remove ^M line 1331 then look for ^M
    sed 's/\r/\\r/g' $loadFile.t > $f.txt

    #replace ^M with newline characters
    sed 's/\r/\n/g' $f.txt > $loadFile.txt

    #this is just for figure, lab, lab_address_update_tracking, publication, updates so far...strip
    #^M inline if necessary.
    sed 's/\r//g' $loadFile.txt > temp

    mv temp $loadFile.txt
    
    #clean up processing steps
    
    rm $loadFile.t
done
