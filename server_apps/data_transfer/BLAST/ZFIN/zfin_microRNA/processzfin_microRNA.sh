#!/bin/tcsh

# this script is really silly; it only does anything important
# when running on genomix.
setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST


$TARGET_PATH/ZFIN/zfin_microRNA/cpzfin_microRNA.sh

#if ($HOSTNAME == genomix.cs.uoregon.edu) then
#   $TARGET_PATH/ZFIN/zfin_microRNA/distributeToNodeszfin_microRNA.sh
#endif 

echo "done copying over the blastdbs for microRNAs."
exit 
