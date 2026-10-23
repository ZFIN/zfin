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

# Exclude data (but keep structure and comments) for the large external_resource tables that are mirrors of
# external data and get reloaded from their sources. Name each such table here. Other tables in the schema are
# dumped with their data, e.g. ncbi_not_in_current_release, which the annotation status of every gene depends on.
${PGBINDIR}/pg_dump -Fc ${DBNAME} --exclude-table-data='external_resource.ncbi_danio_rerio_gene_info' -f $pth/$dirname/`date +"%Y.%m.%d.1"`.bak

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
