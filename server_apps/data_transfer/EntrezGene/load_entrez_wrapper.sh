#! /local/bin/tcsh
# tcsh needed to set env
# load_entrez_wrapper.sh

#private/bin:/private/apps/Informix/informix/bin:/private/ZfinLinks/Commons/bin:/private/bin:/local/bin:/usr/bin:/bin


if ( $1 == "" | $1 == "commit" ) then
	set instance="$MUTANT_NAME"
else
	set instance="$1"
endif

source /private/ZfinLinks/Commons/env/${instance}.env


if ( "$2" == "commit" || "$1" == "commit") then
	gmake run_commit
else
	gmake run
endif
