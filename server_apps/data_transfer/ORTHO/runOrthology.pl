#!/opt/zfin/bin/perl

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;
use Try::Tiny;

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
try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f loadAndUpdateNCBIOrthologs.sql");
} catch {
  warn "Failed to execute loadAndUpdateNCBIOrthologs.sql - $_";
  exit -1;
};

exit;



