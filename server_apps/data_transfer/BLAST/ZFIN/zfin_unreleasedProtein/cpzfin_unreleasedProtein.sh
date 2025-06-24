#!/bin/bash -e
# 
# cps over the unreleasedProtein database from embryonix/helix/
# and replaces the current unreleasedProtein blastdb.
#

rm -f $BLAST_PATH/Backup/unreleasedProtein.xp*
mv -f $BLAST_PATH/Current/unreleasedProtein* $BLAST_PATH/Backup 

#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xpd $BLAST_PATH/Current/
#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xpi $BLAST_PATH/Current/
#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xps $BLAST_PATH/Current/
#cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xpt $BLAST_PATH/Current/


#chmod g+w $BLAST_PATH/Current/*
 
echo "done with cpzfin_unreleasedProtein.sh" ;
exit
