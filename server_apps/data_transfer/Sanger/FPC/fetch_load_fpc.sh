#! /bin/tcsh

set then=`date`
# the database being loaded
set db_server=<!--|DB_NAME|-->@<!--|INFORMIX_SERVER|-->

set recent=`date +.%m.%y.txt`

# to get new files
/local/bin/wget -qN "ftp://ftp.sanger.ac.uk/pub/human/zebrafish/ZFIN/clonemarkers.*$recent"
/local/bin/wget -qN "ftp://ftp.sanger.ac.uk/pub/human/zebrafish/ZFIN/markers.*$recent"
/local/bin/wget -qN "ftp://ftp.sanger.ac.uk/pub/human/zebrafish/ZFIN/ctgnames.*$recent"

# get the date string from the new(est) files
set date=`ls -t1 clonemarkers.*.txt | head -1 | cut -f2,3,4 -d\.`
echo "loading fpc files from $date"

# to adjust the format of the markers file
/private/bin/rebol -sqw marker-to-fpc_zdb.r markers.${date}.txt | grep -v '^$' >! fpc_zdb.unl

# to adjust the format of the other files
cat ctgnames.${date}.txt | sed '/^$/d' | sed 's/*/&\|/g' | cut -c 4- >! fpc_contig.unl
#tailpipe
sed 's/$/\|/g' < clonemarkers.${date}.txt | grep -v '^$'>! fpc_clone.unl

# to move into the database
if ($1 == 'commit') then
    cat update_fpc.sql  commit.sql |dbaccess  $db_server 
else 
    cat update_fpc.sql rollback.sql |dbaccess  $db_server
endif

echo "started $then finished `date`"