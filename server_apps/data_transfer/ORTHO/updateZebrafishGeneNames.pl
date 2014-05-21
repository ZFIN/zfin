#!/private/bin/perl

# updateZebrafishGeneNames.pl
# It parses the input file, updatedZFgeneNames, got from Ken to prepare the update list and then call updateZebrafishGeneNames.sql to do the updating of ZF gene names.
# 

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";

system("/bin/rm -f updated_zf_gene_names.unl");
system("/bin/rm -f existing_zf_gene_names.unl");
system("/bin/rm -f updateZebrafishGeneNameSQLlog1");
system("/bin/rm -f updateZebrafishGeneNameSQLlog2");
system("/bin/rm -f geneNamesUpdatedReport");


open (INPUTNAMES, "updatedZFgeneNames") ||  die "Cannot open updatedZFgeneNames : $!\n";

@lines = <INPUTNAMES>;

close(INPUTNAMES);

open (UPDATEDGENENAMES, ">updated_zf_gene_names.unl") ||  die "Cannot open updated_zf_gene_names.unl : $!\n";

$ct = $ctUpdated = 0;

foreach $line (@lines) {
  $ct++; 
   
  next if $line !~ m/^ZDB\-GENE/ and $line !~ m/^gene name\s*\(z\):/;
    
  if ($line =~ m/(ZDB\-GENE\-\d{6}\-\d+)\s+([a-zA-Z0-9_]+)/) {
      print UPDATEDGENENAMES "$1|$2|";
  } else {
      @fieldsZFgeneLine = split(/gene name\s*\(z\):/, $line); 
      $geneName = $fieldsZFgeneLine[1];
      $geneName =~ s/^\s+//;   #remove leading spaces
      $geneName =~ s/\s+$//; #remove trailing spaces  
      print UPDATEDGENENAMES "$geneName|\n";
      $ctUpdated++;
  }
      
}

print "\ntotal number of lines: $ct\n\n";

print "\ntotal number of lines written to updated_zf_gene_names.unl: $ctUpdated\n\n";

close(UPDATEDGENENAMES);

open (EXISTINGNAMES, "inconsistentZebrafishGeneNamesReport") ||  die "Cannot open inconsistentZebrafishGeneNamesReport : $!\n";

@lines2 = <EXISTINGNAMES>;

close(EXISTINGNAMES);

open (EXISTINGGENENAMES, ">existing_zf_gene_names.unl") ||  die "Cannot open existing_zf_gene_names.unl : $!\n";

foreach $line (@lines2) {
   
  next if $line !~ m/^ZDB\-GENE/ and $line !~ m/^gene name\s*\(z\):/;
    
  if ($line =~ m/(ZDB\-GENE\-\d{6}\-\d+)\s+([a-zA-Z0-9_]+)/) {
      print EXISTINGGENENAMES "$1|$2|";
  } else {
      @fieldsZFgeneLine = split(/gene name\s*\(z\):/, $line); 
      $geneName = $fieldsZFgeneLine[1];
      $geneName =~ s/^\s+//;   #remove leading spaces
      $geneName =~ s/\s+$//; #remove trailing spaces  
      
      print EXISTINGGENENAMES "$geneName|\n" if ZFINPerlModules->stringStartsWithLetterOrNumber($geneName);
  }
      
}

close(EXISTINGGENENAMES);

$cmd = "$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> updateZebrafishGeneNames.sql >updateZebrafishGeneNameSQLlog1 2> updateZebrafishGeneNameSQLlog2";
system($cmd);

system("/bin/cat updateZebrafishGeneNameSQLlog2 >> updateZebrafishGeneNameSQLlog1");

$subject = "Auto from $dbname: " . "updateZebrafishGeneNames.pl :: updateZebrafishGeneNameSQLlog";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","updateZebrafishGeneNameSQLlog1");


$subject = "Auto from $dbname: " . "List of gene names that have been updated based on inputfile by Ken according to NCBI orthology info";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","geneNamesUpdatedReport");


exit;


