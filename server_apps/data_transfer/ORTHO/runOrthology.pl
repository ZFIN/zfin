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
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/NCBIorthology.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parseOrthoFile.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/downloadFiles.pl");
&downloadFiles;
&parseOrthoFiles;
&nameCheck;

;
