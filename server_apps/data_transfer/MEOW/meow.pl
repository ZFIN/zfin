#!/opt/zfin/bin/perl 
#  Script to create files for MEOW database
#  output files are written to <!--|FTP_ROOT|-->/pub/transfer/MEOW/
##$ENV{"DBDATE"}="Y4MD-";

use Try::Tiny;

if (! -e "<!--|FTP_ROOT|-->/pub/transfer/MEOW") {
    system("/bin/mkdir -m 755 -p <!--|FTP_ROOT|-->/pub/transfer/MEOW");
}
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/MEOW";
print "beginning in ". `pwd` ."\n";
system("rm -f ./*.txt");
print "running MEOW_dump.sql on <!--|DB_NAME|-->\n";
try {
  system("$ENV{'PGBINDIR'}/psql <!--|DB_NAME|--> < MEOW_dump.sql");
} catch {
  warn "Failed at MEOW_dump.sql - $_";
  exit -1;
};

system("scp ./*.txt <!--|FTP_ROOT|-->/pub/transfer/MEOW/");

exit;

