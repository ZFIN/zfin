#!/private/bin/perl
#
# Read table format blast result from STANDIN.
# For each subject sequence which is in zfin, query out gene
# zdb id or BAC/PAC zdb id, and added that into the result file.
# For each query, if a hit has >=98% identity, alignment(match)  
# lenth is over 80% of either the query sequence or subject 
# that is a strong candidate.
# If all the other hits that has >=96% identity are all of the same
# gene, and no other hits on the same gene has a <96% identity 
# (no other hits on different genes/BACs/PACs has a >96% identity). then 
# output the clone gene assoication into is_gene.unl file. Otherwise, 
# output to blast2zfin.scnd for manual curation.  
#
# parameter: zfin test/production dbname
#            standin
#
# output: is_gene.unl
#      blast2zfin.scnd:  blast table output that matches at least one zfin gene
#      blast2zfin.third: blast table output that only matches zfin non-gene marker
#
use DBI;

my $usage =<<END;
Usage: 
	filterBlast.pl zfindbname < tableformatblastresultdata
  output: is_gene.unl blast2zfin.scnd
END

die "$usage\n" if (@ARGV < 1);

#=====================================================
# Main
#
# inherit the Informix environment from shell

my $dbname = shift @ARGV;
my $dbh = DBI->connect("DBI:Informix:$dbname", "", "", 
		                    {AutoCommit =>0, PrintError=>1} )
    or die "Failed while connecting to $dbname: $DBI::errstr";

open ISGENE, ">is_gene.unl" or die "Cannot open is_gene.unl file for write.";
open OUT, ">blast2zfin.scnd" or die "Cannot open blast2zfin.scnd file for write.";
open SPEC, ">blast2zfin.third" or die "Cannot open blast2zfin.third file for write.";
 
my $lastQuery = '';
my $hasSuspect = 0;
my $hasGreatSbj = 0;
my $hasGene = "f";
my (@rowArray, $isGene, $theGeneZdbId, @goodSbjMrkrZdbIdListArray);

print OUT "QUERY|SBJCT|ZFIN|IDENTITY|LENTH|MISMCH|QSTART|QEND|SSTART|SEND|EXPECT|SCORE \n";
while (<>) {
    s/\|$//;                 # clean ending
    my ($query, $subject, $percent,$mchLength,$qryLength,
	    $sbjLength, $mismatch, $gap,@restOneRow)= split /\|/;

    output () if ($query ne $lastQuery && $lastQuery);

    my $sbjMrkrZdbIdList = &queryAccession($subject);
    $hasGene = "t" if $sbjMrkrZdbIdList =~ /ZDB-GENE/ ;
    # good subject is expected to be on the same gene
    if ($percent >= 96) {
	
	push @goodSbjMrkrZdbIdListArray, $sbjMrkrZdbIdList;
	
	# identify a great match
	my $shrtLength = ($qryLength > $sbjLength) ? $sbjLength : $qryLength;
	if ($percent >= 98 && $mchLength/$shrtLength > 0.8 && ($sbjMrkrZdbIdList =~ /GENE/)) {
	    
	    $hasGreatSbj = 1;
	    $theGeneZdbId = $sbjMrkrZdbIdList;      # cdna type guarantee one gene in the list
	    $isGene = "$query|$sbjMrkrZdbIdList|";
	}
    }
    elsif ($sbjMrkrZdbIdList eq $theGeneZdbId) {
	$hasSuspect = 1;
    }
    
    $lastQuery = $query;
    push @rowArray, join("|",$query, $subject,$sbjMrkrZdbIdList,$percent,$mchLength,$mismatch,@restOneRow);
}
output ();

close SPEC;
close OUT;
close ISGENE;
exit;

#=========================================================
#  subfunction queryAccession  
#
#  Query zfin database for matching genes   
#   of the input accession number. Matching
#  ESTs/cDNAs are converted to the correspondent genes.
#
#  Input: accession number
#
#  Return: matching GENE/BAC/PAC zdb id list separated by space
#   
#  Output: none
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
                                           'ZDB-FDBCONT-040412-37',
                                           'ZDB-FDBCONT-040412-14',
                                           'ZDB-FDBCONT-060417-1');  
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
# subfunciton output 
#
# Input: 
#    global variable @goodSbjMrkrZdbIdListArray, $theGeneZdbId
#

sub output () {

    my $goodSbjUniq = 1;
    foreach my $goodSbj (@goodSbjMrkrZdbIdListArray) {
		$goodSbjUniq = 0 if $goodSbj ne $theGeneZdbId;
    }

    if ($hasGreatSbj && $goodSbjUniq && !$hasSuspect) {
		print ISGENE "$isGene\n";
    }
    elsif ($hasGene eq "t") {
	        print OUT @rowArray;
    }
    else {
		print SPEC @rowArray;
    }
 
    $hasSuspect = 0;
    $hasGene = "f";
    $hasGreatSbj = 0;
    $theGeneZdbId = "";
    @goodSbjMrkrZdbIdListArray = ();
    $isGene = "";
    @rowArray = ();
}
