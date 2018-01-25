#!/private/bin/perl -T
##
##  fetch the list of db_link that point to vega_withdrawn
##  blast each acc_num against the vega_withdrawn database 
##  report the acc_num not found
##

BEGIN {
    $ENV{PATH}="/local/bin:/usr/bin";
    $ENV{SHELL}="/usr/bin/sh";
    delete $ENV{LD_LIBRARY_PATH};

    #set environment variables
    $ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
    $ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
    $ENV{"ONCONFIG"}="onconfig";
    $ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";
    $ENV{"DBNAME"}="<!--|DB_NAME|-->";
    $ENV{"INTERNAL_BLAST_PATH"}="<!--|INTERNAL_BLAST_PATH|-->";
};


use English;
use feature 'switch';
use DBI;

$usage = "usage: blast_withdrawn.pl \n|INTERNAL_BLAST_PATH| must be set in your .tt file."; 

if ($ENV{"INTERNAL_BLAST_PATH"} !~ /Current/) {

    die "$usage";
}

sub cleanTail ($) {
  my $var = $_[0];

  while ($var !~ /\w$/) {
    chop ($var);
  }

  return $var;
}


$tscriptNotFound = 'false';


### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 

### fetch the withdrawn accession numbers

$sql_command = "select dblink_acc_num from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-100114-1';";

my $cur = $dbh->prepare($sql_command);
my($accNum);
$cur->bind_columns(\$accNum) ;

$cur->execute;

### blast each accession number

while ($cur->fetch) {
    $accNum =~ cleanTail($accNum);
  
    open (BLAST, "/opt/ab-blast/xdget -n /research/zblastfiles/zmore/dev_blastdb/Current/vega_withdrawn $accNum |");

        $line = <BLAST>;
    
        if ($line !~ /$accNum/ ) {
            $tscriptNotFound = 'true';
        }
    
    close (BLAST);
}

### close database connection
$dbh->disconnect();


if ($tscriptNotFound eq 'false')
{
    print "All withdrawn transcripts are in the withdrawn database.\n";
}

exit;
