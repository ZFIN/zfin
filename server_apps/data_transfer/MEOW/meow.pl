#!/local/bin/perl 
#  Script to create files for MEOW database
#  output files are written to <!--|ROOT_PATH|-->/home/transfer/MEOW
$ENV{"DBDATE"}="Y4MD-";
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/MEOW";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> MEOW_dump.sql");


