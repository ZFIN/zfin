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

    system("/local/bin/curl -s ftp://ftp.sanger.ac.uk/pub/kj2/ZFIN/090403/dumped_transcripts_post_buid.fa -o vega_fasta_090403.fa") and die "can not download vega transcript file from sanger";

    print "download done.\n" ;
}


#-----------------------MAIN--------------------#

#remove old files
 
system("/bin/rm -f *.fa") and die "can not rm fasta";

&downloadTscrptFiles();

print "Download Done\n" ;

system("parseVega.pl vega_fasta_090113.fa") and die "can not parse vega fasta file" ;

$count = 0;
$retry = 1;

# wait till parsing is finished

while( !( -e "vega_fasta_090113.unl")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry parseVega.pl\n";
	    system("parseVega.pl vega_fasta_090113.fa");
	}
	else {
	    print ("Failed to run parseVega.pl"); 
	    exit;
	}
    }  
}
print "parsing vegaFasta file done\n";

#------------Loading Database---------------------#

print "loading...\n";

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadVegaTranscripts.sql >out 2> report.txt") and die "loadVegaTranscripts.sql did not complete successfully";

exit;
