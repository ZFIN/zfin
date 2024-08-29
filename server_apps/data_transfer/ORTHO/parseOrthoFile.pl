#!/opt/zfin/bin/perl

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/perl_lib/";
use ZFINPerlModules;

## set environment variables

$dbname = "<!--|DB_NAME|-->";

sub parseOrthoFiles() {
system("rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/ORTHO/parsedOrthos.txt");
open (NCBI, "<!--|TARGETROOT|-->/server_apps/data_transfer/ORTHO/ortholog_info") ||  die "Cannot open ortholog_info : $!\n";
open PARSED, "><!--|TARGETROOT|-->/server_apps/data_transfer/ORTHO/parsedOrthos.txt" or die "Cannot open parsed ortho file : $!\n" ;
$ctLines = $ctHumanGenes = $ctMouseGenes = $ctFlyGenes = 0;

while (<NCBI>) {
  chomp;
  
  @fieldsNCBI = split("\t");

  $taxonId = $fieldsNCBI[0];
  ## excluding species other than human, mouse, fly and the documentation lines
  next if ($taxonId eq "#Format:");
  next if ($taxonId eq "#tax_id");
  $ctLines++;
  
  $ncbiGeneId = $fieldsNCBI[1];
  $symbolNonOfficial = $fieldsNCBI[2];
  $xrefsAll = $fieldsNCBI[5];
  $chromosome = $fieldsNCBI[6];
  $position = $fieldsNCBI[7];
  $symbol = $fieldsNCBI[10];
  $name = $fieldsNCBI[11];
  $lastUpdated = $fieldsNCBI[14];
  
  @xrefs = split(/\|/, $xrefsAll);
  
  foreach $xref (@xrefs) {
      $xref =~ s/HGNC\:HGNC\:/HGNC\:/;
      $xref =~ s/MGI\:MGI\:/MGI\:/;
      @xrefParts = split (/\:/, $xref);
      $xrefDbname = $xrefParts[0];
      $xrefAccNum = $xrefParts[1];
      print PARSED "$taxonId\t$ncbiGeneId\t$chromosome\t$position\t$symbolNonOfficial\t$name\t$xrefDbname\t$xrefAccNum\t$xref\t$lastUpdated\n";
  }
 
}

print "\ntotal number of lines parsed: $ctLines\n";


close(PARSED);

close(NCBI);

return();
}
