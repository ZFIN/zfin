#!/opt/zfin/bin/perl

use DBI;

use lib "$ENV{'ROOT_PATH'}/server_apps/perl_lib/";
use ZFINPerlModules;
use Try::Tiny;

## set environment variables

$dbname = "$ENV{'DB_NAME'}";
require ("$ENV{'ROOT_PATH'}/server_apps/data_transfer/ORTHO/reportOrthoNameChanges.pl");
require ("$ENV{'ROOT_PATH'}/server_apps/data_transfer/ORTHO/parseOrthoFile.pl");
require ("$ENV{'ROOT_PATH'}/server_apps/data_transfer/ORTHO/downloadFiles.pl");
require ("$ENV{'ROOT_PATH'}/server_apps/data_transfer/ORTHO/parseHumanData.pl");

&downloadFiles;
&parseOrthoFiles;
&reportOrthoNameChanges;
&parseHuman;

print "finished parsing and reporting, do updates.\n";
try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $ENV{'DB_NAME'} -a -f loadAndUpdateNCBIOrthologs.sql");
} catch {
  warn "Failed to execute loadAndUpdateNCBIOrthologs.sql - $_";
  exit -1;
};


$cmd = "psql -v ON_ERROR_STOP=1 -d $ENV{'DB_NAME'} -a -f loadHumanSynonyms.sql";
;
 
&doSystemCommand($cmd);

exit;



