#!/private/bin/perl
#
# Read table format blast result from STANDIN.
# For each subject sequence which is in zfin, query out gene
# zdb id or BAC/PAC zdb id, and added that into the result file.
# For each query, if a hit has >=98% identity, alignment(match)  
# lenth is over 80% of either the query sequence or subject, 
# and the subject is cDNA sequence, that is a strong candidate.
# If all the other hits that has >=97% identity are all of the same
# gene, and no other hits on the same gene has a <97% identity and 
# no other hits on different genes/BACs/PACs has a >96% identity. then 
# output the clone gene assoication into is_gene.unl file. Otherwise, 
# output to blast2zfin.scnd for manual curation.  
#
# parameter: zfin test/production dbname
#            standin
#
# output: is_gene.unl
#         blast2zfin.scnd
#
use DBI;

die "Usage: filterBlast.pl zfindbname < tableformatblastresultdata.\n" if (@ARGV < 1);

#=====================================================
# Main
#
# inherit the environment from shell
#$ENV{INFORMIXDIR}      = "/private/apps/Informix/informix_wanda";
#$ENV{INFORMIXSERVER}   = "wanda";
#$ENV{INFORMIXSQLHOSTS} = "$ENV{INFORMIXDIR}/etc/sqlhosts.wanda";
#$ENV{ONCONFIG}         = "onconfig.wanda";

my $dbname = shift @ARGV;
my $dbh = DBI->connect("DBI:Informix:$dbname", "", "", 
		                    {AutoCommit =>0, PrintError=>1} )
    or die "Failed while connecting to $dbname: $DBI::errstr";

open ISGENE, ">is_gene.unl" or die "Cannot open is_gene.unl file for write.";
open OUT, ">blast2zfin.scnd" or die "Cannot open blast2zfin.scnd file for write.";
 
my $lastQuery = '';
my $numSuspects = 0;
my $hasGreatMatch = 0;
my (@rowArray, $isGene, $matchGeneZdbId, @matchGeneZdbIdArray);

print OUT "QUERY|SBJCT|ZFIN|IDENTITY|LENTH|MISMCH|QSTART|QEND|SSTART|SEND|EXPECT|SCORE \n";
while (<>) {
    s/\|$//;                 # clean ending
    my ($query, $subject, $percent,$mchLength,$qryLength,
	    $sbjLength, $mismatch, $gap,@restOneRow)= split /\|/;
    my $sbjType = pop @restOneRow;
    chomp $sbjType;
    my $mrkr_zdb_id = queryAccession($subject);

    output () if ($query ne $lastQuery && $lastQuery);

    if ($percent >= 97) {
	
		push @matchGeneZdbIdArray, $mrkr_zdb_id;
		
		my $shrtLength = ($qryLength > $sbjLength) ? $sbjLength : $qryLength;
		if ($percent >= 98 && $sbjType eq "cdna" && $mchLength/$shrtLength > 0.8 ) {
			
			$hasGreatMatch = 1;
			$matchGeneZdbId = $mrkr_zdb_id;
			$isGene = "$query|$mrkr_zdb_id|";
		}
    }
    elsif ($percent >= 96) {
		$numSuspects ++;
	}
	elsif ($hasGreatMatch && $mrkr_zdb_id ne $matchGeneZdbId) {
		$numSuspects ++;
	}

    $lastQuery = $query;
    push @rowArray, join("|",$query, $subject,$mrkr_zdb_id,$percent,$mchLength,$mismatch,@restOneRow);
}
output ();

close OUT;
close ISGENE;
exit;

#======================================
#  subfunction queryAccession  
#
#  query against zfin database for the 
#  gene zdb id or BAC/PAC zdb id of the 
#  input accession number
#
#  input: accession number
#  return: matching zdb id list
#   
sub queryAccession ($) {
    my $accession = shift;
    my (@match, $match_id, $match_gene_id);
    my $sth = $dbh->prepare("select mrkr_zdb_id
                              from marker, db_link
                             where dblink_acc_num = '$accession'
                               and dblink_linked_recid = mrkr_zdb_id
                               and dblink_fdbcont_zdb_id in (
                                           'ZDB-FDBCONT-040412-36',
                                           'ZDB-FDBCONT-040412-37');
                           ");
    $sth->execute();
    
    while ($match_id = $sth->fetchrow_array) {
	if ($match_id =~ /[EST|CDNA]/) {
	    ($match_gene_id) = $dbh->selectrow_array(
				   "select mrel_mrkr_1_zdb_id
                                      from marker_relationship
                                     where mrel_mrkr_2_zdb_id = '$match_id'
	                               and mrel_type = 'gene encodes small segment';
                                   ");	    
	}	
	push @match, $match_gene_id ? $match_gene_id : $match_id ;
	
    }
    return join (" ", @match);
}

#===============================
#  subfunciton output 
#
sub output () {

    my $matchGeneUniq = 1;
    foreach my $item (@matchGeneZdbIdArray) {
		$matchGeneUniq = 0 if $item ne $matchGeneZdbId;
    }

    if ($hasGreatMatch && $matchGeneUniq && !$numSuspects) {
		print ISGENE "$isGene\n";
    }
    else {
		print OUT join ("\n", @rowArray)."\n";
    }
 
    $numSuspects = 0;
    $hasGreatMatch = 0;
    $matchGeneZdbId = "";
    @matchGeneZdbIdArray = ();
    $isGene = "";
    @rowArray = ();
}
