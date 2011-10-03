#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create a page of markers that is linkable.  
#
# This should create a single html page (markers.html) of the sort:
# <html>
# <header>
# <title>ZFIN Markers</title>
# <keywords>ZFIN,zebrafish,marker, gene, antibody, etc., etc.</keywords>
# </header>
# <body>
# for each marker m{
# <a href="/action/marker/view/${m.zdbID}" title=${m.name}>${m.abbrev}</a> <br>
# }
# </body>
# </html>

use strict ;
use DBI;

use CGI;
my $q = CGI->new ; 


# define GLOBALS

# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ExternalSearch";

my $dbname = "<!--|DB_NAME|-->";
my $user = "";
my $passwd = "";

my $sql = "   select '<a href=\"/action/marker/view/'||m.mrkr_zdb_id || '\" title=\"'||m.mrkr_name||'\">' || m.mrkr_abbrev || '</a><br>'  
from marker m   
order by m.mrkr_type, m.mrkr_abbrev ; 
" ;

my $dbh = DBI->connect("DBI:Informix:$dbname", $user, $passwd) 
       or die "Cannot connect to Informix database $dbname:$DBI::errstr\n";

my $sth = $dbh->prepare($sql) or die "SQL prepare failed. $sql";
$sth->execute() or die "SQL execute failed.+ $sql ";

my $fileWithHTMLtags = '<!--|ROOT_PATH|-->/home/zf_info/markers.html';
print "dumping to $fileWithHTMLtags\n" ;
open RESULT,  ">$fileWithHTMLtags" or die "Can't open: $fileWithHTMLtags $!\n";

print RESULT $q->start_html(
    {
        -type=>'text/html',
        -expires=>'+1w',
        -title=>'ZFIN Markers',
        -meta=>{
            'keywords'=>'Antibody,BAC,BAC END,cDNA,Engineered Foreign Gene,EST,Enhancer Trap Construct,Fosmid,Gene,Gene Family,Pseudogene,Gene Trap Construct,Morpholino,Mutant,PAC,PAC END,Promoter Trap Construct,RAPD,Engineered Region,SNP,SSLP,STS,Transgenic Construct,Transcript'
        }
    }
);

while (my @row = $sth ->fetchrow_array()){
  print RESULT @row ; 
}

print RESULT $q->end_html ; 

close RESULT;
