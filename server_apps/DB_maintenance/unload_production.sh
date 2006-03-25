#! /bin/tcsh

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:/private/ZfinLinks/Commons/bin:$PATH

set pth=/research/zunloads/databases/<!--|DB_NAME|-->
set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

# A while back unloads from production to the NFS mounted development RAID
# started taking a really long time, as much as 8 times longer than 
# unloading the DB directly on to production disk and then copying it.
# Therefore, to speed up the process from over 4 hours to well under
# an hour, do the unload to production disk and then copy it.

if ($HOST != "embryonix") then
  /private/ZfinLinks/Commons/bin/unloaddb.pl <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname
  /bin/cp -pr <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname $pth/$dirname
  /bin/rm -rf <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname
else 
  /private/ZfinLinks/Commons/bin/unloaddb.pl <!--|DB_NAME|--> $pth/$dirname
endif

chgrp -R fishadmin $pth/$dirname
chmod -R g+rw $pth/$dirname
