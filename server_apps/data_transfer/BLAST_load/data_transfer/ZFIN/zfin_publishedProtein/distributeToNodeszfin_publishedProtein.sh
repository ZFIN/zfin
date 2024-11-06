#!/bin/tcsh
#
# Scp publishedProtein curated sequence from embryonix,
# update blast db.

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/publishedProtein.* node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif

echo "done with distributeToNodeszfin_publishedProtein.sh" ;

exit
