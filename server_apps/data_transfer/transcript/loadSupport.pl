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
sub downloadTscrptFiles () { # download the obo file from GO

    system("/local/bin/curl -s ftp://ftp.sanger.ac.uk/pub/kj2/ZFIN/090306/evidence.txt -o evidence.txt") and die "can not download evidence.txt from sanger";

    print "download done.\n" ;
}


#-----------------------MAIN--------------------#

#remove old files
 
#system("/bin/rm -f *.fa") and die "can not rm fasta";

&downloadTscrptFiles();

print "Download Done\n" ;

system("parseSuppt.pl evidence.txt") and die "can not parse vega fasta file" ;

$count = 0;
$retry = 1;

# wait till parsing is finished

while( !( -e "evidence.unl")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry parseSuppt.pl\n";
	    system("parseSuppt.pl evidence.txt");
	}
	else {
	    print ("Failed to run parseSuppt.pl"); 
	    exit;
	}
    }  
}
print "parsing evidence file done\n";


exit;
