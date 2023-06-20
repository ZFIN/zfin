#!/opt/zfin/bin/perl 

# validate_prob_files.pl
# quick check on prob files to see if the records already exist in our DB with other references

use strict;
use warnings;
use DBI;
use POSIX;

my $dbname = $ENV{'DB_NAME'};
my $dbhost = $ENV{'PGHOST'};
my $username = "";
my $password = "";

### open a handle on the db
my $dbh = DBI->connect("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

my %global_cache = ();

sub main {

    #print header
    print "file,record,exists already,pub_count,gene_count,pub_ids,gene_ids,dblink_ids,pub_names,gene_abbrevs,accession,dblink_infos,fdb_db_name\n";

    my $files = [1..10];
    foreach my $file (@$files) {
        my $filename = "prob$file";
        # print "$filename\n";
        check_file($filename);
    }
}

sub check_file {
    my $filename = shift;
    my $record_count = 1;
    $/ = "\/\/\n"; #custom record separator
    open INPUT, $filename or die "Cannot open $filename";
    foreach my $record (<INPUT>) {
        my $ac_line;
        my @ac = ();
        my $ac_count = 0;

        #split lines by newline
        my @lines = split /\n/, $record;

        #check each line
        foreach my $line (@lines) {
            #check for ID line
            if ($line =~ /^AC\s+(.*)/) {
                $ac_line = $1;
                @ac = split /;\s+/, $ac_line;

                #remove trailing semicolon
                foreach (@ac) {
                    $_ =~ s/;$//;
                }

                $ac_count = @ac;
                #check each AC line
                check_acs(\@ac, $filename, $record_count);
            }
        }
        if (!$ac_count) {
            #print "$filename: No AC lines found in record $record_count\n";
        }
        $record_count++;
    }
}

sub cache_uniprot_entries {
    if (%global_cache) {
        return;
    }

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

sub check_acs {
    cache_uniprot_entries();

    my @ac = @{$_[0]};
    my $filename = $_[1];
    my $record_count = $_[2];

    # print "  $filename: '@ac' \n";
    foreach my $ac (@ac) {
        my $cache_hit_ref = $global_cache{$ac};
        if (!$cache_hit_ref) {
            print "  $filename,$record_count,$ac,NOT EXISTS\n";
            next;
        }
        my @cache_hit = @$cache_hit_ref;
        #join array into string
        my $line = join ',', @cache_hit;
        print "  $filename,$record_count,$ac,EXISTS,$line\n";
    }
}

main();
