#!/private/bin/perl
#
# This script generates ZFIN Genomic accession and 
# After Sunday night's almost reload, we schedule this script. 
#

use strict;

# define environment variable
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/sqlhosts";

my $dbname = "<!--|DB_NAME|-->";

my $username = "";
my $password = "";
my $outputdir ="";

if ($dbname ne "almdb") {
    $outputdir = "<!--|WEBHOST_FASTA_FILE_PATH|-->" ;
}
else {
    $outputdir = "<!--|WEBHOST_FASTA_FILE_PATH|-->" ;    
}

my $accFileRefSeq = $outputdir."zfin_genomic_refseq_acc.unl";
my $accFileGenBank = $outputdir."zfin_genomic_genbank_acc.unl";

################
# Form the sql 
################

my $sqlrefseq ="unload to \"$accFileRefSeq\" delimiter \" \" 
                 select dblink_acc_num 
                   from db_link 
                   where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id 
                                                     from foreign_db_contains, foreign_db, foreign_db_data_type 
                                                     where fdbdt_data_type = \"Genomic\" 
                                                     and fdbdt_super_type = \"sequence\" 
                                                     and fdbcont_fdbdt_id = fdbdt_pk_id 
                                                     and fdbcont_fdb_db_id = fdb_db_pk_id 
                                                     and fdb_db_name = \"RefSeq\")";  

my $sqlgenbank ="unload to \"$accFileGenBank\" delimiter \" \" 
                   select dblink_acc_num 
                     from db_link 
                     where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id 
                                                       from foreign_db_contains, foreign_db, foreign_db_data_type 
                                                       where fdbdt_data_type =\"Genomic\" 
                                                       and fdbdt_super_type = \"sequence\" 
                                                       and fdbcont_fdbdt_id = fdbdt_pk_id 
                                                       and fdbcont_fdb_db_id = fdb_db_pk_id 
                                                       and fdb_db_name =\"GenBank\")";  
  

#################
# Execute
#################
system ("echo '$sqlrefseq' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sqlrefseq \n"; 

system ("echo '$sqlgenbank' | $ENV{INFORMIXDIR}/bin/dbaccess $dbname -") && die "Failure on sql: $sqlgenbank \n"; 

exit;
