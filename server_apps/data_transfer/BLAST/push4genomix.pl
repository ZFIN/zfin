#!/private/bin/perl -W
#
# push4genomix.pl
#
# This script queries out zfin accession numbers
# and their related genes/clones. Information
# is outputed to '|' delimited file to zfin ftp
# site for genomix.cs.uoregon.edu to download.
#
# NOTE: THIS FILE IS DEPRECATED.  However, it may be useful to have this as a local table in the future.
#

use DBI;

my ($output_acc_num, $output_mrkr_1_display, $output_mrkr_1_oid, $output_mrkr_2_display, $output_mrkr_2_oid,%is_clone,%is_smallseg, $mtype);

# define environment variable
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my $outputdir = "<!--|FTP_ROOT|-->/pub/transfer/Genomix";
my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

my $dbh = DBI->connect("DBI:Informix:$dbname", $username, $password)
	or die "Cannot connect to Informix database: $DBI::errstr\n" ;

# put CLONE marker types into a hash
my $sql = "
               select mtgrpmem_mrkr_type
                 from marker_type_group_member
                where mtgrpmem_mrkr_type_group = 'CLONE'
              ";
my $sth = $dbh->prepare($sql);
$sth -> execute();
undef %is_clone;

while (($mtype) = $sth->fetchrow_array) {
       $is_clone{$mtype} =1;
}

# put SMALLSEG marker types into a hash
$sql = "
               select mtgrpmem_mrkr_type
                 from marker_type_group_member
                where mtgrpmem_mrkr_type_group = 'SMALLSEG'
              ";
$sth = $dbh->prepare($sql);
$sth -> execute();
undef %is_smallseg;
while (($mtype) = $sth->fetchrow_array) {
    $is_smallseg{$mtype} =1;
}

&generateAccMrkrRelationshipFile();
&generateGeneDataFile();

$dbh->disconnect();

exit;
#================================
# sub generateGeneDataFile

sub generateGeneDataFile () {
    my $sql = "
	select mrkr_zdb_id
          from marker
         where mrkr_type like 'GENE%'";

    my $sth = $dbh->prepare($sql);
    $sth -> execute();

    open OUT, ">$outputdir/gene_related_data.txt"
	or die "Cannot open file to write $! \n";

    my $gene_zdb_id = '';

    while (($gene_zdb_id) = $sth->fetchrow_array) {
	my $has_xpat = 0;
        my $has_xpat_img = 0;
	my $has_go = 0;
	my $has_pato = 0;
	my $has_pato_img = 0;

        # query xpat fig
	$sql = "
                select xpatfig_fig_zdb_id
                  from expression_experiment, expression_result,
                       expression_pattern_figure, image
                 where xpatex_zdb_id = xpatres_xpatex_zdb_id
                   and xpatex_gene_zdb_id = '$gene_zdb_id'
                   and xpatres_zdb_id  = xpatfig_xpatres_zdb_id
                   and xpatfig_fig_zdb_id = img_fig_zdb_id";

	if ($dbh->selectrow_array ($sql)) {
	    $has_xpat_img = 1;
	    $has_xpat = 1;
	}

	# if no fig, query if any xpat
	if (!$has_xpat_img) {

	    $sql = "
                select xpatres_zdb_id
                  from expression_experiment, expression_result
                 where xpatex_zdb_id = xpatres_xpatex_zdb_id
                   and xpatex_gene_zdb_id = '$gene_zdb_id' ";

	    $has_xpat = 1 if $dbh->selectrow_array ($sql);
	}

        # query GO info
	$sql = "
                select mrkrgoev_zdb_id
                  from marker_go_term_evidence
                 where mrkrgoev_mrkr_zdb_id = '$gene_zdb_id' ";

	$has_go = 1 if $dbh->selectrow_array ($sql);


        # query phenotype fig
	$sql = "
                select phenox_fig_zdb_id
                  from mutant_fast_search, phenotype_experiment, figure, image
                 where mfs_mrkr_zdb_id = '$gene_zdb_id'
                   and mfs_genox_zdb_id = apato_genox_zdb_id
                   and phenox_fig_zdb_id = img_fig_zdb_id";

	if ($dbh->selectrow_array ($sql)) {
	    $has_pato_img = 1;
	    $has_pato = 1;
	}

	# if no fig, query if any pato
	if (!$has_pato_img) {

	    $sql = "
                select phenox_fig_zdb_id
                  from mutant_fast_search, phenotype_experiment
                 where mfs_mrkr_zdb_id = '$gene_zdb_id'
                   and mfs_genox_zdb_id = phenox_genox_zdb_id       ";

	    $has_pato = 1 if $dbh->selectrow_array ($sql);
	}

	print OUT "$gene_zdb_id|$has_xpat|$has_xpat_img|$has_go|$has_pato|$has_pato_img|\n";

    }

    close OUT;

}

#==========================================================
# sub generateAccMrkrRelationshipFile
#
# get acc and mrkr from db_link, restrict foreign dbs to
# VEGA_TRANS, GenBank, GenPept, UniProt, RefSeq

sub generateAccMrkrRelationshipFile() {

    # gether 1) accession info
    #        2) morpholino info
    #        3) microRNA info
    my $sql = "
        select distinct dblink_acc_num as acc_num, mrkr_zdb_id, mrkr_abbrev, mrkr_type
          from db_link, marker
         where dblink_linked_recid = mrkr_zdb_id
           and dblink_fdbcont_zdb_id in (
	'ZDB-FDBCONT-040412-36',  --GenBank DNA
	'ZDB-FDBCONT-040412-37',  --GenBank RNA
	'ZDB-FDBCONT-040412-38',  --RefSeq  RNA
    'ZDB-FDBCONT-040527-1',   --RefSeq  DNA
	'ZDB-FDBCONT-040412-39',  --RefSeq  AA
	'ZDB-FDBCONT-040412-42',  --GenPept AA
	'ZDB-FDBCONT-040412-47',  --UniProt AA
	'ZDB-FDBCONT-060417-1',   --Vega_Trans Transcript
	'ZDB-FDBCONT-071128-1',   --unreleasedRNA	   Transcript
	'ZDB-FDBCONT-050210-1'    --PREVEGA    Transcript
)
       union
        select mrkrseq_mrkr_zdb_id as acc_num, mrkr_zdb_id, mrkr_abbrev, mrkr_type
          from marker_sequence, marker_relationship, marker
         where mrkrseq_mrkr_zdb_id = mrel_mrkr_1_zdb_id
           and mrel_mrkr_2_zdb_id = mrkr_zdb_id
           and mrel_type = 'knockdown reagent targets gene'
       union
        select mrkr_zdb_id as acc_num, mrkr_zdb_id, mrkr_abbrev, mrkr_type
          from marker_sequence, marker
         where mrkrseq_mrkr_zdb_id = mrkr_zdb_id
           and mrkrseq_mrkr_zdb_id like 'ZDB-GENE%' -- this catches Pesudo Genes as well

       order by acc_num, mrkr_zdb_id ";

    my $sth = $dbh->prepare($sql);
    $sth -> execute();

    open OUT, ">$outputdir/accession_marker_relationship.txt"
	or die "Cannot open file to write $! \n";

    my $gene_count = 0;
    my $last_acc_num = '';
    my $acc_num = '';
    my $mrkr_zdb_id = '';
    my $mrkr_abbrev = '';
    my $mrkr_type = '';

    while (($acc_num, $mrkr_zdb_id, $mrkr_abbrev, $mrkr_type) = $sth->fetchrow_array) {

	if ($acc_num ne $last_acc_num) {

	    print OUT "$output_acc_num|$output_mrkr_1_display|$output_mrkr_1_oid|$output_mrkr_2_display|$output_mrkr_2_oid|\n" if ($last_acc_num && $output_mrkr_1_display) ;

	    $last_acc_num = $acc_num;
	    $gene_count = 0;
	    $output_acc_num = $acc_num;
	    $output_mrkr_1_display = '';
	    $output_mrkr_1_oid = '';
	    $output_mrkr_2_display = '';
	    $output_mrkr_2_oid = '';
	}

	if ( $is_clone{$mrkr_type} ) {

	    my $sql = "select mrel_mrkr_2_zdb_id, mrkr_abbrev
                        from marker_relationship, marker
                       where mrel_mrkr_1_zdb_id = '$mrkr_zdb_id'
                         and mrel_type = 'clone contains gene'
                         and mrel_mrkr_2_zdb_id = mrkr_zdb_id  ";
	    &queryGeneAndDisplay ($acc_num, $mrkr_zdb_id, $mrkr_abbrev, $sql);
	}
	elsif ( $is_smallseg{$mrkr_type} ) {

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
		$output_mrkr_1_oid = "$mrkr_zdb_id";

	    }else {
		$output_mrkr_1_display = $gene_count." Genes";
		$output_mrkr_1_oid = $output_acc_num;
	    }

	}

    } # end while

    print OUT "$output_acc_num|$output_mrkr_1_display|$output_mrkr_1_oid|$output_mrkr_2_display|$output_mrkr_2_oid|\n" if $output_mrkr_1_display;

    close OUT;
}

########################################################
# queryGeneAndDisplay
#
# input:
#       accession #
#       clone zdb id
#       clone abbrev
#       sql to query related gene
# output:
#       set $output_mrkr_1_display $output_mrkr_1_oid
#           $output_mrkr_2_display $output_mrkr_2_oid
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
	$output_mrkr_1_oid = $gene_zdb_id;
	$output_mrkr_2_display = $o_abbrev;
	$output_mrkr_2_oid = $o_id;
    }
    elsif ( $gene_count > 1 ) {

	$output_mrkr_1_display = $gene_count." Genes";
	$output_mrkr_1_oid = $acc;
	$output_mrkr_2_display = $o_abbrev;
	$output_mrkr_2_oid = $o_id;

    }
    else {
	$output_mrkr_1_display = $o_abbrev;
	$output_mrkr_1_oid = $o_id;

    }

}
return;

