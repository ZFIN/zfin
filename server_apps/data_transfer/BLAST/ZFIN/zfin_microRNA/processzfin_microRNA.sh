#!/bin/tcsh

# this script is really silly; it only does anything important
# when running on genomix.


@TARGET_PATH@/ZFIN/zfin_microRNA/cpzfin_microRNA.sh

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/ZFIN/zfin_microRNA/distributeToNodeszfin_microRNA.sh
#endif 

echo "done copying over the blastdbs for microRNAs."
exit 
