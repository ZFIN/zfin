#!/private/bin/perl 
#  Script to create files for MEOW database
#  output files are written to <!--|FTP_ROOT|-->/pub/transfer/MEOW/
$ENV{"DBDATE"}="Y4MD-";
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
if (! -e "<!--|FTP_ROOT|-->/pub/transfer/MEOW") {
    system("/bin/mkdir -m 755 -p <!--|FTP_ROOT|-->/pub/transfer/MEOW");
}
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/MEOW";
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> MEOW_dump.sql");


