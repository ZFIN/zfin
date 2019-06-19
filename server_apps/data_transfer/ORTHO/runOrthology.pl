#!/opt/zfin/bin/perl

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$dbname = "<!--|DB_NAME|-->";
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/reportOrthoNameChanges.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parseOrthoFile.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/downloadFiles.pl");

&downloadFiles;
&parseOrthoFiles;
&reportOrthoNameChanges;

print "finished parsing and reporting, do updates.\n";
$cmd = "psql -d <!--|DB_NAME|--> -a -f loadAndUpdateNCBIOrthologs.sql";
;

&doSystemCommand($cmd);
