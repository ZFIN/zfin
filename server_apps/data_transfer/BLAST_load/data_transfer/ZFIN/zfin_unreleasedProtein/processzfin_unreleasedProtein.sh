#!/bin/tcsh
# this script is really silly; it only does anything important
# when running on genomix.


echo "start of the processzfin_unreleasedProtein.sh" ;

 @TARGET_PATH@/ZFIN/zfin_unreleasedProtein/cpzfin_unreleasedProtein.sh

 
echo "done with processzfin_unreleasedProtein.sh";

exit
