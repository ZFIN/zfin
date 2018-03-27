#!/bin/tcsh

set pth=/research/zunloads/databases/${DBNAME}
set dirname=`date +"%Y.%m.%d.1"`

${PGBINDIR}/psql $DBNAME ${SOURCEROOT}/server_apps/DB_maintenance/set_unload_timestamp.sql

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

${PGBINDIR}/pg_dumpall --clean --verbose --no-role-passwords >  $pth/$dirname
if ($? != "0") then
  /bin/rm -rf $pth/$dirname
else 
  chgrp -R fishadmin $pth/$dirname
  chmod -R g+rw $pth/$dirname

  chgrp -R fishadmin $pth/$dirname
  chmod -R g+rw $pth/$dirname

  echo "pg_dumpall completed successfully."
endif
