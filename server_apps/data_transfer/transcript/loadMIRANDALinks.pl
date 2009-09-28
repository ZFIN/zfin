#!/private/bin/perl 

use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# make sure in the right directory

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/";
chdir "$dir";
print "$dir"."\n" ;

# SubRoutines #
sub downloadTscrptFiles () { # download the MIRANDA file from Sanger

    system("/local/bin/curl -s ftp://ftp.sanger.ac.uk/pub/mirbase/targets/v5/arch.v5.txt.danio_rerio.zip -o arch.v5.txt.danio_rerio.zip") and die "can not download MIRANDA transcript file from sanger";

    system("/local/bin/unzip arch.v5.txt.danio_rerio.zip");
 
    print "download done.\n" ;
}


#-----------------------MAIN--------------------#

#remove old files
 
system("/bin/rm -f *.zip") and die "can not rm arc.v5.txt.danio_rerio.zip";

system("/bin/rm -f v5.txt.danio_rerio") and die "can not rm arc.v5.txt.danio_rerio.zip";

&downloadTscrptFiles();

print "Download Done\n" ;

system("parseMIRANDA.pl v5.txt.danio_rerio") and die "can not parse sanger MIRANDA file" ;

$count = 0;
$retry = 1;

# wait till parsing is finished

while( !( -e "v5.txt.danio_rerio")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry parseMIRANDA.pl\n";
	    system("parseMIRANDA.pl v5.txt.danio_rerio");
	}
	else {
	    print ("Failed to run parseMIRANDA.pl"); 
	    exit;
	}
    }  
}
print "parsing MIRANDA file done\n";

#------------Loading Database---------------------#

print "loading...\n";

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadMIRANDALinks.sql >out 2> report.txt") and die "loadMIRANDALinks.sql did not complete successfully";

exit;
