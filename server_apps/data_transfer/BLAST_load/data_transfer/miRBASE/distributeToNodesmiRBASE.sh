#!/bin/tcsh
#
# Scp Morpholino sequence and
# microRNA sequence from embryonix,
# update blast db.

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAMature.xnd node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAMature.xni node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAMature.xns node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAMature.xnt node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current

   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAStemLoop.xnd node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAStemLoop.xni node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAStemLoop.xns node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/LoadedMicroRNAStemLoop.xnt node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current

 end

endif 

exit
