#!/bin/tcsh
# this script is really silly; it only does anything important
# when running on genomix.


echo "start of the processzfin_publishedProtein.sh" ;

 @TARGET_PATH@/ZFIN/zfin_publishedProtein/cpzfin_publishedProtein.sh

 
echo "done with processzfin_publishedProtein.sh";

exit
