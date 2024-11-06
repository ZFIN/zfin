#!/bin/tcsh
#
# Scp unreleasedProtein curated sequence from embryonix,
# update blast db.

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/unreleasedProtein.* node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif

echo "done with distributeToNodeszfin_unreleasedProtein.sh" ;

exit
