#!/bin/bash -e
#
# cp over/process curated blastdbs and/or dbs created by zfin acc 
# nums to genomix.
# 

#source /research/zusers/blast/BLAST_load/properties/current;

echo "process zfin_mrph" ;
zfin_mrph/processzfin_mrph.sh

echo "";
echo "";

echo "process zfin_publishedProtein" ;
zfin_publishedProtein/processzfin_publishedProtein.sh

echo "";
echo "";

echo "process zfin_unreleasedProtein" ;
zfin_unreleasedProtein/processzfin_unreleasedProtein.sh

echo "";
echo "";

echo "process zfin_vega" ;
zfin_vega/processzfin_vega.sh

echo "";
echo "";

echo "process vega_withdrawn" ;
zfin_vegaWithdrawn/processzfin_vegaWithdrawn.sh

echo "";
echo "";

echo "process zfin_cdna" ;
zfin_cdna/processzfin_cdna.sh

echo "";
echo "";

echo "process zfin_xpat_cdna" ;
zfin_xpat_cdna/processzfin_xpat_cdna.sh

echo "";
echo "";

echo "process zfin_unreleasedRNA" ;
zfin_unreleasedRNA/processzfin_unreleasedRNA.sh

echo "";
echo "";

echo "process zfin_microRNA" ;
zfin_microRNA/processzfin_microRNA.sh

echo "";
echo "";

echo "process zfin_genomicDNA" ;
zfin_genomicDNA/processzfin_genomicDNA.sh

echo "";
echo "";

echo "process zfin_publishedRNA" ;
zfin_publishedRNA/processzfin_publishedRNA.sh

echo "";
echo "";

echo "process zfin_crispr" ;
zfin_crispr/processzfin_crispr.sh

echo "";
echo "";

echo "process zfin_talen" ;
zfin_talen/processzfin_talen.sh

echo "";
echo "";

echo "cp all ZFIN daily updates to almost" ;

if (@HOSTNAME@ == zygotix.zfin.org  && @WEBHOST_BLAST_DATABASE_PATH@ == /research/zfin.org/blastdb) then 
    
    # clean out the production files and replace with the newly updated ones.
    # except for the curated blastdbs; this seems too dangerous to do.

    echo "rm the backup copy that existed before today"
    cd @WEBHOST_BLAST_DATABASE_PATH@/Backup/
    rm -f zfin_mrph*
    rm -f vega_zfin*
    rm -f zfin_cdna*
    rm -f ZFINGenesWithExpression*
    rm -f GenomicDNA*
    rm -f zfin_talen*
    rm -f zfin_crispr*

    echo "mv the current files to a backup"
    cd @WEBHOST_BLAST_DATABASE_PATH@/Current/
    mv -f zfin_mrph* ../Backup
    mv -f vega_zfin* ../Backup
    mv -f zfin_cdna* ../Backup
    mv -f ZFINGenesWithExpression* ../Backup
    mv -f GenomicDNA* ../Backup
    mv -f zfin_talen* ../Backup
    mv -f zfin_crispr* ../Backup

    echo "cp the new files over"
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_mrph* .
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vega_zfin* .
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_cdna* .
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ZFINGenesWithExpression* .
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/GenomicDNA* .
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_talen* .
    cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_crispr* .

    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_mrph*
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_zfin*
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_cdna*
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/ZFINGenesWithExpression*
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/GenomicDNA*
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_talen*
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_cripsr*

    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_mrph*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/vega_zfin*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_cdna*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/ZFINGenesWithExpression*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/GenomicDNA*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_talen*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/zfin_crispr*
  
endif


exit
