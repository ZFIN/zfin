#!/private/bin/perl 
#
# push4genomix.pl
#
# This script queries out zfin accession numbers 
# and their related genes/clones. Information
# is outputed to '|' delimited file to zfin ftp
# site for genomix.cs.uoregon.edu to download.
#
  
use DBI;

my ($dbh, $sql, $sth, $acc_num, $mrkr_zdb_id, $mrkr_abbrev, $gene_count, $last_acc_num, $output_acc_num, $output_mrkr_1_display, $output_mrkr_1_url, $output_mrkr_2_display, $output_mrkr_2_url);

# define environment variable
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my $outputdir = "<!--|FTP_ROOT|-->/pub/transfer/Genomix";
my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

my $zfin_mrkr_url = "http://zfin.org/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=";
my $zfin_qry_gene_acc_url = "http://zfin.org/cgi-bin/webdriver?MIval=aa-markerselect.apg&marker_type=GENE&query_results=t&input_acc=";

$dbh = DBI->connect("DBI:Informix:$dbname", $username, $password) 
	or die "Cannot connect to Informix database: $DBI::errstr\n" ;

# get acc and mrkr from db_link, restrict dbs to 
# Sanger_FPC, VEGA, GenBank, RefSeq, Ensembl, Sanger clone, 
# GenPept, SwissProt, RefSeq, VEGA_clone

$sql = "select dblink_acc_num as acc_num, mrkr_zdb_id, mrkr_abbrev
          from db_link, marker
         where dblink_linked_recid = mrkr_zdb_id 
           and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-10','ZDB-FDBCONT-040412-14',
                                         'ZDB-FDBCONT-040412-36','ZDB-FDBCONT-040412-37',
                                         'ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39',
                                         'ZDB-FDBCONT-040917-2','ZDB-FDBCONT-040412-41',
                                         'ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-47',
                                         'ZDB-FDBCONT-040527-1','ZDB-FDBCONT-040826-2',
                                         'ZDB-FDBCONT-060417-1')
       union
        select mrkrseq_mrkr_zdb_id as acc_num, mrkr_zdb_id, mrkr_abbrev
          from marker_sequence, marker_relationship, marker
         where mrkrseq_mrkr_zdb_id = mrel_mrkr_1_zdb_id
           and mrel_mrkr_2_zdb_id = mrkr_zdb_id
           and mrel_type = 'knockdown reagent targets gene'
       order by acc_num, mrkr_zdb_id ";
           
$sth = $dbh->prepare($sql);
$sth -> execute();
	
open OUT, ">$outputdir/acc_mrkr_link.txt" 
	or die "Cannot open file to write $! \n";

$gene_count = 0;
$last_acc_num = '';

while (($acc_num, $mrkr_zdb_id, $mrkr_abbrev) = $sth->fetchrow_array) {
	
	if ($acc_num ne $last_acc_num) {

		print OUT "$output_acc_num|$output_mrkr_1_display|$output_mrkr_1_url|$output_mrkr_2_display|$output_mrkr_2_url|\n" if ($last_acc_num && $output_mrkr_1_display) ;
		
		$last_acc_num = $acc_num;
		$gene_count = 0;
		$output_acc_num = $acc_num;
		$output_mrkr_1_display = '';
		$output_mrkr_1_url = '';
		$output_mrkr_2_display = '';
		$output_mrkr_2_url = '';
	}
	
	if ( $mrkr_zdb_id =~ /-BAC-|-PAC-/ ) {
		
		my $sql = "select mrel_mrkr_2_zdb_id, mrkr_abbrev
                        from marker_relationship, marker
                       where mrel_mrkr_1_zdb_id = '$mrkr_zdb_id'
                         and mrel_type = 'clone contains gene'
                         and mrel_mrkr_2_zdb_id = mrkr_zdb_id  ";
			&queryGeneAndDisplay ($acc_num, $mrkr_zdb_id, $mrkr_abbrev, $sql);
	}		
	elsif ( $mrkr_zdb_id =~ /EST|CDNA/ ) {	   
		
		my $sql = "select mrel_mrkr_1_zdb_id, mrkr_abbrev
                        from marker_relationship, marker
                       where mrel_mrkr_2_zdb_id = '$mrkr_zdb_id'
                         and mrel_type in ('gene encodes small segment',
                                         'gene contains small segment')
                         and mrel_mrkr_1_zdb_id = mrkr_zdb_id "; 
		
		&queryGeneAndDisplay($acc_num, $mrkr_zdb_id, $mrkr_abbrev, $sql);
	} 
	
	elsif ( $mrkr_zdb_id =~ /GENE/ )  {
		$gene_count ++;
		if ($gene_count == 1) {
			
			$output_mrkr_1_display = $mrkr_abbrev;
			$output_mrkr_1_url = $zfin_mrkr_url."$mrkr_zdb_id";
			
		}else {
			$output_mrkr_1_display = $gene_count." Genes";
			$output_mrkr_1_url = $zfin_qry_gene_acc_url.$output_acc_num;
		}
		
	}
	
} # end while

print OUT "$output_acc_num|$output_mrkr_1_display|$output_mrkr_1_url|$output_mrkr_2_display|$output_mrkr_2_url|\n" if $output_mrkr_1_display;

close OUT;
exit;

########################################################
# queryGeneAndDisplay 
# 
# input:
#       accession #
#       clone zdb id
#       clone abbrev
#       sql to query related gene
# output:
#       set $output_mrkr_1_display $output_mrkr_1_url
#           $output_mrkr_2_display $output_mrkr_2_url
#
sub queryGeneAndDisplay () {
	
    my $acc = $_[0];
    my $o_id = $_[1];
    my $o_abbrev = $_[2];
    my $sql = $_[3];

    my $sec_array_ref = $dbh->selectall_arrayref($sql);
    my $gene_count = @$sec_array_ref;

	if ( $gene_count == 1 ) {
		my $row = $sec_array_ref->[0];
		
		my $gene_zdb_id = $row->[0];
		my $gene_abbrev = $row->[1]; 
			   		
		$output_mrkr_1_display = $gene_abbrev;
		$output_mrkr_1_url = $zfin_mrkr_url.$gene_zdb_id;
		$output_mrkr_2_display = $o_abbrev;
		$output_mrkr_2_url = $zfin_mrkr_url.$o_id;
	}
	elsif ( $gene_count > 1 ) {

		$output_mrkr_1_display = $gene_count." Genes";
		$output_mrkr_1_url = $zfin_qry_gene_acc_url.$acc;
		$output_mrkr_2_display = $o_abbrev;
		$output_mrkr_2_url = $zfin_mrkr_url.$o_id;

	}
    else {
		$output_mrkr_1_display = $o_abbrev;
		$output_mrkr_1_url = $zfin_mrkr_url.$o_id;

    }
    
}
return;

