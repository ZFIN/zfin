#!/opt/zfin/bin/perl 
#  Script to create files for ZIRC to load
#  output files are written to <!--|ROOT_PATH|-->/home/data_transfer/ZIRC/

use Try::Tiny;
use lib "<!--|ROOT_PATH|-->/server_apps/perl_lib/";
use ZFINPerlModules;

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters";
umask(022);

try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d <!--|DB_NAME|--> -f pushToZirc.sql");
} catch {
  warn "Failed to execute pushToZirc.sql - $_";
  exit -1;
};

