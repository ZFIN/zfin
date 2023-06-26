#!/opt/zfin/bin/perl
package ProblemFileUtil;

use strict;
use MIME::Lite;
use DBI;
use Exporter 'import';

our @EXPORT_OK = qw(getAllExistingUniprotRecordsByAccession);

my %global_cache = ();

#
# collect a report of uniprot entries in the database
# includes the associated genes, publications, and dblinks
#
sub cache_uniprot_entries {
    if (%global_cache) {
        return;
    }

    my $dbname = $ENV{'DB_NAME'};
    my $dbhost = $ENV{'PGHOST'};
    my $username = "";
    my $password = "";

    ### open a handle on the db
    my $dbh = DBI->connect("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
        or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

    #($pub_id, $gene_id, $dblink_id, $title, $marker, $accession, $dblink_info, $fdb_name)
    my $sql = "WITH fullreport as (SELECT
	recattrib_source_zdb_id as pub_id,
	dblink_linked_recid as gene_id,
	dl.dblink_zdb_id as dblink_id,
	title as publication,
	mrkr_abbrev as gene_abbrev,
	dblink_acc_num as accession,
	dblink_info,
	fdb_db_name
FROM
	record_attribution ra
	LEFT JOIN db_link dl ON ra.recattrib_data_zdb_id = dl.dblink_zdb_id
	left join foreign_db_contains fc on dl.dblink_fdbcont_zdb_id = fc.fdbcont_zdb_id
	left join foreign_db fdb on fc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
	left join publication p on p.zdb_id = recattrib_source_zdb_id
	left join marker m on dl.dblink_linked_recid = m.mrkr_zdb_id
WHERE
	fdb.fdb_db_name = 'UniProtKB')
select count(pub_id) as pub_count, count(gene_id) as gene_count, string_agg(pub_id, ';') as pub_ids, string_agg(gene_id, ';') as gene_ids, string_agg(dblink_id, ';') as dblink_ids, string_agg(publication, ';') as pub_names, string_agg(gene_abbrev, ';') as gene_abbrevs, accession, string_agg(COALESCE(dblink_info,''), ';') as dblink_infos, fdb_db_name from fullreport
group by accession, fdb_db_name
order by count(gene_id) desc, count(pub_id) desc";

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    while (my @row = $sth->fetchrow_array) {
        my $acc = $row[7];

        #check for array in cache
        if (!exists($global_cache{$acc})) {
            $global_cache{$acc} = \@row;
        } else {
            print "Second entry for $acc\n";
            die;
        }
    }
}

sub getAllExistingUniprotRecordsByAccession {
    cache_uniprot_entries();
    return \%global_cache;
}

1;