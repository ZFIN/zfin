#!/bin/bash -e
# this script is really silly; it only does anything important
# when running on genomix.

source "../config.sh"
log_message "start of the processzfin_unreleasedProtein.sh" ;

./cpzfin_unreleasedProtein.sh

log_message "done with processzfin_unreleasedProtein.sh";

exit
