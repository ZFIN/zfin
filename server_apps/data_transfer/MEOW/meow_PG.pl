#!/private/bin/perl 
#  Script to create files for MEOW database
#  output files are written to <!--|FTP_ROOT|-->/pub/transfer/MEOW/
##$ENV{"DBDATE"}="Y4MD-";

if (! -e "<!--|FTP_ROOT|-->/pub/transfer/MEOW") {
    system("/bin/mkdir -m 755 -p <!--|FTP_ROOT|-->/pub/transfer/MEOW");
}
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/MEOW";
print "beginning in ". `pwd` ."\n";
system("rm -f ./*.txt");
print "running MEOW_dump.sql on <!--|DB_NAME|-->\n";
system("$ENV{'PGBINDIR'}/psql <!--|DB_NAME|--> < MEOW_dump_PG.sql");
system("scp ./*.txt <!--|FTP_ROOT|-->/pub/transfer/MEOW/");

exit;
