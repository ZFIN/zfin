#!/bin/tcsh -e

set pth=/research/zunloads/databases/${INSTANCE}
mkdir -p $pth

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

# Leave out the rows, but not the structure or comments, of the tables that load_file marks
# lf_exclude_from_dump: mirrors of external data that the loads recreate. Every other table is dumped with its
# rows, including ncbi_not_in_current_release, which the annotation status of every gene depends on.
# If load_file cannot be read, nothing is excluded.
set excludes = ( `${SOURCEROOT}/server_apps/DB_maintenance/dump_exclude_args.sh ${DBNAME}` )
echo "pg_dump leaves out the rows of: $excludes"
${PGBINDIR}/pg_dump -Fc ${DBNAME} $excludes -f $pth/$dirname/`date +"%Y.%m.%d.1"`.bak

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
