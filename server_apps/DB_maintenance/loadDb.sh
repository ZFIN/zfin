#!/bin/tcsh -e

# $1 = dbName
# $2 = dumpDirectory
# $3 = directory to gmake
# $4 = env file to use in gmake

#echo dbName $1
#echo dumpDir $2
#echo directory to gmake $3
#echo env file $4
source $4

echo "command /private/ZfinLinks/Commons/bin/loaddb.pl $1 /bin/ls -1t $2 | head -1" ;

# reload db
cd $2
set dumpToUse = `/bin/ls -1t | head -1` 

/private/ZfinLinks/Commons/bin/loaddb.pl $1 $dumpToUse

cd $3

# gmake postloaddb
/local/bin/gmake postloaddb
exit
