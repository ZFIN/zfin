#!/local/bin/perl 
#  Script to create files for John Postlethwait for checking SC sequence data
#  output files are written to <!--|ROOT_PATH|-->/home/transfer/JohnP
$ENV{"DBDATE"}="Y4MD-";
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/JohnP";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> create_files_for_johnp.sql");


