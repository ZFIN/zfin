#!/opt/zfin/bin/perl 
#  Script to create files for ZIRC to load
#  output files are written to <!--|ROOT_PATH|-->/home/data_transfer/ZIRC/

use Try::Tiny;

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters";
umask(022);

try {
  system("${PGBINDIR}/psql <!--|DB_NAME|--> pushToZirc.sql");
} catch {
  warn "Failed to execute pushToZirc.sql - $_";
  exit -1;
};

