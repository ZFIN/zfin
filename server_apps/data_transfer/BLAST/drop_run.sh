#! /bin/tcsh
# i.e.
# drop_run.sh ZDB-RUN-080514-2
# drop_run.sh ZDB-RUN-080514-2 commit

#drop a run
set run = "$1"

echo "deleting $run"

set terminate = "rollback"

if("commit" == $2) then
 set terminate = "$2"
endif

dbaccess -a $DBNAME << END
begin work;
 delete from zdb_active_data where zactvd_zdb_id = "$run";
$terminate work;
END
