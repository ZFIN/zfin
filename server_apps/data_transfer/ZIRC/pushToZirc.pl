#!/private/bin/perl 
#  Script to create files for ZIRC to load
#  output files are written to <!--|FTP_ROOT|-->/pub/transfer/ZIRC
$ENV{"DBDATE"}="Y4MD-";
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> pushToZirc.sql");


