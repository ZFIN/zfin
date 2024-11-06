#!/bin/tcsh
#
# Scp curated microRNA sequence from embryonix,
# update blast db.

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

# Curated mature

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xnd node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xni node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xns node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xnt node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

# Curated Stem Loop

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xnd node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xni node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xns node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xnt node${i}.cluster.private:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif

echo "done distributing to nodes CuratedMicroRNA.*"


exit
