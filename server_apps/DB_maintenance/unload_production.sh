#!/bin/tcsh -e

set pth=/research/zunloads/databases/${INSTANCE}
set dirname=`date +"%Y.%m.%d.1"`

${SOURCEROOT}/server_apps/DB_maintenance/set_unload_timestamp.sh

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

echo "pg_dumpall starting"

${PGBINDIR}/pg_dumpall --clean --verbose --no-role-passwords >  $pth/`date +"%Y.%m.%d.1"`.dumpall


mkdir $pth/$dirname

echo "pg_dump starting"

${PGBINDIR}/pg_dump -Fc ${DBNAME} -f $pth/$dirname/`date +"%Y.%m.%d.1"`.bak

if ($? != "0") then
  /bin/rm -rf $pth/$dirname
  echo "unload_production failed"
else 
  chgrp -R fishadmin $pth/$dirname
  chmod -R g+rw $pth/$dirname

  chgrp -R fishadmin $pth/$dirname
  chmod -R g+rw $pth/$dirname

  echo "pg_dumpall completed successfully."
endif
