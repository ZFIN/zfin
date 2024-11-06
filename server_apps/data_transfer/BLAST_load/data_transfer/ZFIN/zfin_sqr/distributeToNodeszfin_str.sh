#!/bin/tcsh
#
# Scp STR sequence from embryonix,
# update blast db.

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_str.* node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current

 end

endif 

exit
