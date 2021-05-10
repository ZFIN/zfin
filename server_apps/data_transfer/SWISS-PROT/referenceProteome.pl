#!/opt/zfin/bin/perl

##
## referenceProteome.pl
##
## ZFIN-294  DLOAD-497
## 
## download and parse the data file of reference proteome from UniProt and run SQLs to get the lists of
## genes that don't have any protein accession but have xpat or phenotype
## genes that have a protein ID of any kind
## genes that have a protein ID of any kind and have xpat or phenotype
## genes that have a protein ID of any kind and have xpat or phenotype and not associated with reference proteome

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;
use Try::Tiny;

# set environment variables
$ENV{"DBDATE"}="Y4MD-";
$ENV{"CLIENT_LOCALE"}="en_US.utf8";
$ENV{"DB_LOCALE"}="en_US.utf8";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/";

system("/bin/rm -f *.tab");
system("/bin/rm -f UP000000437_7955.fasta");

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$sqlGetGenesNoProteinButHavingXpatOrPheno = "select distinct mrkr_abbrev, mrkr_zdb_id 
                                               from marker where mrkr_type = 'GENE' 
                                                and not exists(select 'x' from db_link, foreign_db_contains, foreign_db 
                                                                where fdbcont_fdbdt_id = 2 
                                                                  and fdbcont_organism_common_name = 'Zebrafish' 
                                                                  and mrkr_zdb_id = dblink_linked_recid 
                                                                  and dblink_fdbcont_zdb_id = fdbcont_zdb_id 
                                                                  and fdb_db_pk_id = fdbcont_fdb_db_id) 
                                                and (exists(select 'x' from expression_experiment where xpatex_gene_zdb_id = mrkr_zdb_id) 
                                                  or exists(select 'x' from mutant_fast_Search where mfs_data_zdb_id = mrkr_zdb_id))
                                                order by mrkr_abbrev;";


$curGetGenesNoProteinButHavingXpatOrPheno = $dbh->prepare_cached($sqlGetGenesNoProteinButHavingXpatOrPheno);

$curGetGenesNoProteinButHavingXpatOrPheno->execute();

$curGetGenesNoProteinButHavingXpatOrPheno->bind_columns(\$sym, \$id);

open NOPRBUTXPATORPHENO, ">genesNoProteinButWithXpatOrPheno.tab" || die ("Cannot open genesNoProteinButWithXpatOrPheno.tab !");
$ctGenesNoProteinButWithXpatOrPheno = 0;
while ($curGetGenesNoProteinButHavingXpatOrPheno->fetch()) {
    print NOPRBUTXPATORPHENO "$sym\t$id\n";
    $ctGenesNoProteinButWithXpatOrPheno++;
}

$curGetGenesNoProteinButHavingXpatOrPheno->finish();

print "\nNumber of genes that don't have any protein accession but have xpat or phenotype: $ctGenesNoProteinButWithXpatOrPheno\n";

close(NOPRBUTXPATORPHENO); 

$sqlGetGenesWithProteinID = "select distinct mrkr_abbrev, mrkr_zdb_id
                                        from marker where mrkr_type = 'GENE'
                                         and exists(select 'x' from db_link, foreign_db_contains, foreign_db
                                                     where fdbcont_fdbdt_id = 2
                                                       and fdbcont_organism_common_name = 'Zebrafish'
                                                       and mrkr_zdb_id = dblink_linked_recid
                                                       and dblink_fdbcont_zdb_id = fdbcont_zdb_id
                                                       and fdb_db_pk_id = fdbcont_fdb_db_id)
                                    order by mrkr_abbrev;";


$curGetGenesWithProteinID = $dbh->prepare_cached($sqlGetGenesWithProteinID);

$curGetGenesWithProteinID->execute();

$curGetGenesWithProteinID->bind_columns(\$sym, \$id);

open WITHPR, ">genesWithProteinAcc.tab" || die ("Cannot open genesWithProteinAcc.tab !");
$ctGenesWithProteinID = 0;
while ($curGetGenesWithProteinID->fetch()) {
    print WITHPR "$sym\t$id\n";
    $ctGenesWithProteinID++;
}

$curGetGenesWithProteinID->finish();

print "\nNumber of genes that have a protein ID of any kind: $ctGenesWithProteinID\n";

close(WITHPR);

$sqlGetGenesWithProteinAndXpatOrPheno = "select distinct mrkr_abbrev, mrkr_zdb_id
                                           from marker where mrkr_type = 'GENE'
                                            and exists(select 'x' from db_link, foreign_db_contains, foreign_db
                                                        where fdbcont_fdbdt_id = 2
                                                          and fdbcont_organism_common_name = 'Zebrafish'
                                                          and mrkr_zdb_id = dblink_linked_recid
                                                          and dblink_fdbcont_zdb_id = fdbcont_zdb_id
                                                          and fdb_db_pk_id = fdbcont_fdb_db_id)
                                            and (exists(select 'x' from expression_experiment where xpatex_gene_zdb_id = mrkr_zdb_id)
                                              or exists(select 'x' from mutant_fast_Search where mfs_data_zdb_id = mrkr_zdb_id))
                                       order by mrkr_abbrev;";


$curGetGenesWithProteinAndXpatOrPheno = $dbh->prepare_cached($sqlGetGenesWithProteinAndXpatOrPheno);

$curGetGenesWithProteinAndXpatOrPheno->execute();

$curGetGenesWithProteinAndXpatOrPheno->bind_columns(\$sym, \$id);

open WITHPRANDXPATORPHENO, ">genesWithProteinAndWithXpatOrPheno.tab" || die ("Cannot open genesWithProteinAndWithXpatOrPheno.tab !");
$ctGenesWithProteinAndXpatOrPheno = 0;
while ($curGetGenesWithProteinAndXpatOrPheno->fetch()) {
    print WITHPRANDXPATORPHENO "$sym\t$id\n";
    $ctGenesWithProteinAndXpatOrPheno++;
}

$curGetGenesWithProteinAndXpatOrPheno->finish();

print "\nNumber of genes that have a protein ID of any kind and have xpat or phenotype: $ctGenesWithProteinAndXpatOrPheno\n";

close(WITHPRANDXPATORPHENO);

system("/local/bin/wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000437_7955.fasta.gz");

system("/local/bin/gunzip UP000000437_7955.fasta.gz");

$/ = '>';

open(FASTA, "<UP000000437_7955.fasta") or die "Couldn't open file UP000000437_7955.fasta, $!";
open REFPR, ">refProteome.tab" || die ("Cannot open refProteome.tab !");
while(<FASTA>) {
  $line = $_;
  if ($line) {
  chomp($line);
  @fields = split(/\|/, $line);
  print REFPR "$fields[1]|\n";
  undef @fields;
  }
}

close(REFPR);

try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f referenceProteome.sql");
} catch {
  warn "Failed to execute referenceProteome.sql - $_";
  exit -1;
};

exit;


