#!/opt/zfin/bin/perl

# oneTimeOrthoInconsistencyReport.pl (ZFIN-10286)
#
# One-shot scan for ZFIN gene names that disagree with the Human and/or Mouse
# ortholog names ALREADY IN the ortholog table (ortho_other_species_name).
# Same substring + chopped-name relaxation rule as the weekly report's
# inconsistency check, but applied without the alreadyExamined allow-list so
# every current inconsistency surfaces.
#
# Output: oneTimeOrthoInconsistencyReport.tsv next to the script.
# Columns: gene_zdb_id, zfin_symbol, zfin_name, organism, ortho_symbol, ortho_name
#
# Not part of the weekly Update-Orthology_w pipeline. Run by hand:
#     cd server_apps/data_transfer/ORTHO && ./oneTimeOrthoInconsistencyReport.pl
# Override target DB with PGHOST / PGDATABASE env vars (DBI::Pg picks them up
# automatically). Placeholder defaults are still honored when the script is
# deployed via the standard template-substitution path.

use strict;
use warnings;
use DBI;

my $dbname = $ENV{PGDATABASE} || "<!--|DB_NAME|-->";
my $dbhost = $ENV{PGHOST}     || "<!--|PGHOST|-->";
my $output = "oneTimeOrthoInconsistencyReport.tsv";

my $dbh = DBI->connect("DBI:Pg:dbname=$dbname;host=$dbhost", $ENV{PGUSER} // "", $ENV{PGPASSWORD} // "")
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

# Restricted to Human + Mouse (per task description; fly excluded). Pulls the
# orthologue name AS STORED at ZFIN so the comparison reflects the orthology
# section currently visible on gene pages.
my $sql = qq{
    select organism_common_name,
           ortho_other_species_name,
           ortho_other_species_symbol,
           ortho_zebrafish_gene_zdb_id,
           mrkr_abbrev,
           mrkr_name
      from ortholog, marker, organism
     where ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
       and mrkr_type = 'GENE'
       and organism_taxid = ortho_other_species_taxid
       and organism_common_name in ('Human','Mouse')
     order by mrkr_abbrev
};

my $cur = $dbh->prepare($sql);
$cur->execute();

open(my $out, ">", $output) or die "Cannot open $output : $!\n";
print $out join("\t", qw(gene_zdb_id zfin_symbol zfin_name organism ortho_symbol ortho_name)), "\n";

my $inconsistent = 0;
my $total = 0;

while (my ($organism, $orthoName, $orthoSymbol, $zdbId, $zfinSymbol, $zfinName) = $cur->fetchrow_array) {
    $total++;
    next unless defined $orthoName && length $orthoName;

    # Mirror the weekly inconsistency rule: case-insensitive substring check
    # in both directions, allowing the trailing-letter strip for zebrafish
    # paralog naming ("mybphb" vs "MYBPH").
    my $zfNorm    = lc($zfinName);     $zfNorm    =~ s/,//g;
    my $orthoNorm = lc($orthoName);    $orthoNorm =~ s/,//g;
    my $zfChopped = $zfNorm;           chop $zfChopped;

    next if $zfNorm =~ m/\Q$orthoNorm/;
    next if $orthoNorm =~ m/\Q$zfChopped/;

    $inconsistent++;
    print $out join("\t", $zdbId, $zfinSymbol, $zfinName, $organism, $orthoSymbol // "", $orthoName), "\n";
}

$cur->finish();
$dbh->disconnect();
close($out);

print "Scanned $total Human/Mouse orthologs; $inconsistent inconsistencies written to $output\n";
