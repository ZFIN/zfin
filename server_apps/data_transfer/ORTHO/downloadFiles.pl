#!/private/bin/perl

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$dbname = "<!--|DB_NAME|-->";



sub downloadFiles() {
open LOG, '>', "logOrthologyUpdateName" or die "can not open logOrthologyUpdateName: $! \n";

## clean up after last run of this script
doSystemCommand("/bin/rm -f *.txt");
doSystemCommand("/bin/rm -f logOrthologyUpdateName");
doSystemCommand("/bin/rm -f ortholog_info");
doSystemCommand("/bin/rm -f orthNamesUpdatedReport");
doSystemCommand("/bin/rm -f updateGeneNamesReport");
doSystemCommand("/bin/rm -f inconsistentZebrafishGeneNamesReport");
doSystemCommand("/bin/rm -f ncbiIdsNotFoundReport");
doSystemCommand("/bin/rm -f orthNamesUpdateList.unl");
doSystemCommand("/bin/rm -f updateOrthologyNameSQLlog1");
doSystemCommand("/bin/rm -f updateOrthologyNameSQLlog2");

doSystemCommand("rm -f Homo_sapiens.gene_info.gz");
doSystemCommand("/bin/rm -f Homo_sapiens.gene_info");
doSystemCommand("/bin/rm -f Mus_musculus.gene_info");
doSystemCommand("/bin/rm -f Drosophila_melanogaster.gene_info");

doSystemCommand("scp /research/zarchive/load_files/Orthology/alreadyExamined <!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/")  if (!-e "alreadyExamined");

## download new copies of files of interest and get them organized, unpacked, etc...
doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz");
doSystemCommand("/local/bin/gunzip Homo_sapiens.gene_info.gz");
doSystemCommand("/bin/cp Homo_sapiens.gene_info ortholog_info");

doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Mammalia/Mus_musculus.gene_info.gz");
doSystemCommand("/local/bin/gunzip Mus_musculus.gene_info.gz");
doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Invertebrates/Drosophila_melanogaster.gene_info.gz");
doSystemCommand("/local/bin/gunzip Drosophila_melanogaster.gene_info.gz");

doSystemCommand("/bin/cat Mus_musculus.gene_info >> ortholog_info");
doSystemCommand("/bin/cat Drosophila_melanogaster.gene_info >> ortholog_info");
return ();

}

sub doSystemCommand {

  my $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";
    
  my $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) { 
     
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";
     
     close LOG;
     exit;
  }
}
