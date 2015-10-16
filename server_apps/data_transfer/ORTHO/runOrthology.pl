#!/private/bin/perl

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/reportOrthoNameChanges.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parseOrthoFile.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/downloadFiles.pl");

&downloadFiles;
&parseOrthoFiles;
&reportOrthoNameChanges;

print "finished parsing and reporting, do updates.\n";
$cmd = "$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> loadAndUpdateNCBIOrthologs.sql";
;

open ORTHOSTATS, "><!--|TARGETROOT|-->/server_apps/data_transfer/ORTHO/orthoStats.txt" or die "Cannot open orthostats file : $!\n" ;

$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

$sqlStats =
'select count(*) as counter, "HGCNC Links", fdb_db_name
   from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "HGNC"
   group by fdb_db_name
  union
 select count(*) as counter, "MGI Links", fdb_db_name
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "MGI"
 group by fdb_db_name
  union
 select count(*) as counter, "OMIM Links", fdb_db_name
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "OMIM"
 group by fdb_db_name
  union
 select count(*) as counter, "Gene Links", fdb_db_name
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "Gene"
 group by fdb_db_name';

$cur = $dbh->prepare($sqlStats);
$cur->execute();
$cur->bind_columns(\$count,\$db,\$fdbname);

while ($cur->fetch()) {
    print ORTHOSTATS $count." ".$db."\n";
}

close(ORTHOSTATS);
&doSystemCommand($cmd);
