#!/bin/tcsh -e

source <!--|SOURCEROOT|-->/commons/env/<!--|INSTANCE|-->.env
cd <!--|SOURCEROOT|-->/server_apps/data_transfer/SWISS-PROT/
/local/bin/gmake run ;

cd <!--|SOURCEROOT|-->/server_apps/data_transfer/GO
/local/bin/gmake run ;

