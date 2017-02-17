#!/bin/bash

startTime=$(date)
echo $startTime

cd /research/zunloads/databases/trunkdb/

latestDump=`ls -td -- */ | head -n 1 | cut -d'/' -f1`
echo $latestDump


echo "*** Removing previous working directories"
rm -rf /research/zunloads/databases/postgres_dumps/$latestDump;

cp -R /research/zunloads/databases/trunkdb/$latestDump /research/zunloads/databases/postgres_dumps/
cd /research/zunloads/databases/postgres_dumps/

echo "*** latest dump: "
echo $unloadDirectory
unloadDirectory=/research/zunloads/databases/postgres_dumps/$latestDump

cd $unloadDirectory

rm unload_*
rm *.err
rm *.out
rm btsfse*
rm done
#not sure why these two didn't come thru in the schema file
rm paneled_markers
rm pub_db_xref
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
    sed 's/\r/\n/g' $f.t > $loadFile.txt

    #this is just for figure, lab, lab_address_update_tracking, publication, updates so far...strip
    #^M inline if necessary.
    sed 's/\r//g' $loadFile.txt > temp

    mv temp $loadFile.txt
    
    #clean up processing steps
    
    rm $loadFile.t
done


