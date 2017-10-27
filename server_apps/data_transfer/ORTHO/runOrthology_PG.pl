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
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/reportOrthoNameChanges_PG.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parseOrthoFile_PG.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/downloadFiles.pl");

&downloadFiles;
&parseOrthoFiles;
&reportOrthoNameChanges;

print "finished parsing and reporting, do updates.\n";
$cmd = "psql -d <!--|DB_NAME|--> -a -f loadAndUpdateNCBIOrthologs_PG.sql";
;

&doSystemCommand($cmd);
