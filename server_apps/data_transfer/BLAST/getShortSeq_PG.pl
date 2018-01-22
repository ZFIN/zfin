#!/private/bin/perl
#
# This script generates ZFIN locally stored Morpholino
# and microRNA sequences for Genomix to download. 
# It will be a nightly cron job at embrynoix.
#

use strict;
use DBI;

#===========================================
# Main
#

# define environment variable

my $dbname = "<!--|DB_NAME|-->";
my $user = "";
my $passwd = "";
my $outputdir ="";

# a place on embryonix/zygotix is used to store the fasta files for blast db update.
if ($dbname ne "almdb") {
    $outputdir = "<!--|WEBHOST_FASTA_FILE_PATH|-->/" ;
}
else {
    $outputdir = "<!--|WEBHOST_FASTA_FILE_PATH|-->/" ;    
}

my $mrphFile = $outputdir."zfin_mrph.fa";


my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $user, $passwd) or die "Cannot connect to database: $DBI::errstr\n";

my $sql;
#======  regenerate morpholino sequence set =======
$sql =    " select mrel_mrkr_1_zdb_id, mrph.mrkr_name, seq_sequence
              from marker_sequence 
                   join marker_relationship 
                         on seq_mrkr_zdb_id = mrel_mrkr_1_zdb_id
                   join marker mrph
                         on mrel_mrkr_1_zdb_id = mrph.mrkr_zdb_id
                   join marker gn
                         on mrel_mrkr_2_zdb_id = gn.mrkr_zdb_id";

&formFastaFile ($mrphFile, $sql);

exit;


#===========================================
# formFastaFile 
#
sub formFastaFile ($$) {
    my $resultFasta = $_[0];
    my $execSql = $_[1];

    open FA, ">$resultFasta" 
	or die "Cannot open the file to write result.";

    my $sth = $dbh->prepare($execSql) or die "SQL prepare failed.";
    $sth->execute() or die "SQL execute failed.";

    while(my ($mrkr_id, $mrkr_name, $mrkr_seq)=$sth->fetchrow_array()){

	print FA ">$mrkr_id $mrkr_name   \n";
	print FA "$mrkr_seq\n\n";
    }

    close FA;
}
