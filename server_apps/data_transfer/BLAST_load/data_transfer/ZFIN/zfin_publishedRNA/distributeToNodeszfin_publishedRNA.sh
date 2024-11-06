#!/bin/tcsh
#
# Scp publishedRNA to nodes

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/publishedRNA.* node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current

 end

endif

echo "done with distributeToNodeszfin_publishedRNA.sh" ;

exit
