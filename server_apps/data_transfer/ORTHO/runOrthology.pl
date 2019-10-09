#!/opt/zfin/bin/perl

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$dbname = "<!--|DB_NAME|-->";
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/reportOrthoNameChanges.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parseOrthoFile.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/downloadFiles.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parseHumanData.pl");

&downloadFiles;
&parseOrthoFiles;
&reportOrthoNameChanges;
&parseHuman;

print "finished parsing and reporting, do updates.\n";
$cmd = "psql -d <!--|DB_NAME|--> -a -f loadAndUpdateNCBIOrthologs.sql";
;

&doSystemCommand($cmd);

$cmd = "psql -d <!--|DB_NAME|--> -a -f loadHumanSynonyms.sql";
;

&doSystemCommand($cmd);
