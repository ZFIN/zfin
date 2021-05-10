#!/bin/tcsh

set pth=/research/zunloads/databases/${DBNAME}
set dirname=`date +"%Y.%m.%d.1"`


if (! (-d $pth/$dirname) ) then
      echo "unloaddb.pl did NOT complete successfully. Missing unload for production!"
 endif

