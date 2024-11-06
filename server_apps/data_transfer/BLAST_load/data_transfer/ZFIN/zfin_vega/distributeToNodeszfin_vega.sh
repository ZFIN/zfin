#!/bin/tcsh
#
# for Vega, sync the nodes

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vega* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current

endif 

echo "done with distributeToNodeszfin_vega.sh" ;
exit
