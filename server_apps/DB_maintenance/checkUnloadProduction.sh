#!/bin/tcsh

setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:/opt/zfin/bin:$PATH
setenv ENVIRONMENT <!--|ENVIRONMENT|-->

set pth=/research/zunloads/databases/<!--|DB_NAME|-->
set dirname=`date +"%Y.%m.%d.1"`


if ($ENVIRONMENT != "development") then
     if (! (-d $pth/$dirname) ) then
      echo "unloaddb.pl did NOT complete successfully. Missing unload for production!"
     endif
endif


