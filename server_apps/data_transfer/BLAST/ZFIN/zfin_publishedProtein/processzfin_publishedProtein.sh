#!/bin/bash -e
# this script is really silly; it only does anything important
# when running on genomix.


source "../config.sh"
log_message "***** Starting ZFIN cDNA Sequences *****"


./zfin_publishedProtein/cpzfin_publishedProtein.sh

 
log_message "done with processzfin_publishedProtein.sh";

exit
