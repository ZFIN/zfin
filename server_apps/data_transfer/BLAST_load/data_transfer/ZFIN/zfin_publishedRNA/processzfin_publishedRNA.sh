#!/bin/tcsh
# this script is really silly; it only does anything important
# when running on genomix.


echo "start of the processzfin_publishedRNA.sh" ;

 @TARGET_PATH@/ZFIN/zfin_publishedRNA/cpzfin_publishedRNA.sh

 
echo "done with processzfin_publishedRNA.sh";

exit
