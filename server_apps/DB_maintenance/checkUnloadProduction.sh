#!/bin/tcsh

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:/private/ZfinLinks/Commons/bin:$PATH

set pth=/research/zunloads/databases/<!--|DB_NAME|-->
set dirname=`date +"%Y.%m.%d.1"`


if ($HOST != "zygotix") then
     if (! (-d $pth/$dirname) ) then
      echo "unloaddb.pl did NOT complete successfully. Missing unload for production!"
     endif
endif


