#!/bin/bash

/bin/rm /research/zfin.org/blastdb/Backup/all_refprot_aa*
/bin/mv /research/zfin.org/blastdb/Current/all_refprot_aa* /research/zfin.org/blastdb/Backup/

/bin/rm /research/zfin.org/blastdb/Backup/ensemb* 
/bin/mv /research/zfin.org/blastdb/Current/ensembl* /research/zfin.org/blastdb/Backup/

/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl*.x* /research/zfin.org/blastdb/Current/
/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/all_refprot_aa.* /research/zfin.org/blastdb/Current/

/bin/rm /research/zfin.org/blastdb/Backup/sptr* 
/bin/mv /research/zfin.org/blastdb/Current/sptr* /research/zfin.org/blastdb/Backup/

/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/sptr*.x* /research/zfin.org/blastdb/Current/

/bin/rm /research/zfin.org/blastdb/Backup/zfinEnsemblTscript* 
/bin/mv /research/zfin.org/blastdb/Current/zfinEnsemblTscript* /research/zfin.org/blastdb/Backup/


/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfinEnsemblTscript*.x* /research/zfin.org/blastdb/Current/

/bin/rm /research/zfin.org/blastdb/Backup/vegaprotein* 
/bin/mv /research/zfin.org/blastdb/Current/vegaprotein* /research/zfin.org/blastdb/Backup/


/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vegaprotein*.x* /research/zfin.org/blastdb/Current/

/bin/rm /research/zfin.org/blastdb/Backup/refseq* 
/bin/mv /research/zfin.org/blastdb/Current/refseq* /research/zfin.org/blastdb/Backup/


/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq*.x* /research/zfin.org/blastdb/Current/

/bin/chmod 664 /research/zfin.org/blastdb/Current/*
/bin/chgrp zfishweb /research/zfin.org/blastdb/Current/*
