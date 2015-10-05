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

$cmd ="$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> loadAndUpdateNCBIOrthologs.sql > updateOrthologySQLlog1 2> updateOrthologySQLlog2";
;

&doSystemCommand($cmd);
