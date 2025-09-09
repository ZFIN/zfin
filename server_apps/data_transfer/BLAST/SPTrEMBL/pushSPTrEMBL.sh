#!/bin/bash -e
#
# Script moves formatedd/converted blastdb files to the production dir.

source "../config.sh"
echo "==| Cp to db dir SPTrEMBL |=="

cp sptr_*.xp* $BLAST_PATH/Current/

#/bin/chgrp zfishweb $WEBHOST_BLAST_DATABASE_PATH/Current/sptr_*.x*
#/bin/chmod 664 $WEBHOST_BLAST_DATABASE_PATH/Current/sptr_*.x*

/bin/rm sptr_*.xp*

echo "==| Done with SPTrEMBL |=="

exit
