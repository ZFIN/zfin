#! /bin/tcsh
# tcsh needed to set env
# load_entrez_wrapper.sh

#private/bin:/private/apps/Informix/informix/bin:/private/ZfinLinks/Commons/bin:/private/bin:/local/bin:/usr/bin:/bin
source /private/ZfinLinks/Commons/env/beaky.env

if ( "$1" == "commit" )  then
	gmake run_commit
else
	gmake run
endif
