#!/bin/tcsh -e

if (-e @TARGET_PATH@/missingSequencesReport.txt) then
    /bin/rm -f @TARGET_PATH@/missingSequencesReport.txt
endif

/bin/rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/lengths*

cd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

/bin/wc -c *.xnd | /bin/sed -e 's/^[ \t]*//' | /bin/sed 's/ /|/' | /bin/sed 's/$/|/' | /bin/sed 's/\.xnd//' > @BLASTSERVER_BLAST_DATABASE_PATH@/Current/lengths.txt

@INFORMIXDIR@/bin/dbaccess -a @DBNAME@ @TARGET_PATH@/checkLengths.sql

set size = `ls -l missingSequencesReport.txt | awk '{print $5}'`

if ($size>0) then

    /local/bin/mutt -a @BLASTSERVER_BLAST_DATABASE_PATH@/Current/missingSequencesReport.txt -s "ERROR: blastdbs are missing sequences" -- @EMAIL@ < @TARGET_PATH@/char

endif
