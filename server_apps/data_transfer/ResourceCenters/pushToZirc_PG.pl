#!/private/bin/perl 
#  Script to create files for ZIRC to load
#  output files are written to <!--|ROOT_PATH|-->/home/data_transfer/ZIRC/

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters";
umask(022);
system("${PGBINDIR}/psql <!--|DB_NAME|--> pushToZirc_PG.sql");
