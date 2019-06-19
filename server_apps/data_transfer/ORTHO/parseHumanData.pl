#!/opt/zfin/bin/perl

#set environment variables

system("rm -f updateHumanOrthologyLog1");
system("rm -f updateHumanOrthologyLog2");
system("rm -f Homo_sapiens.gene_info.gz");
###system("rm -f Homo_sapiens.gene_info");
system("rm -f hum_chr_loc_sym_mim.tab");

if (!-e "Homo_sapiens.gene_info") {
  system("wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz");
  system("gunzip Homo_sapiens.gene_info.gz");
}

$ctHumanLines = $ctMissingMIM = 0;

open (HUMAN, "Homo_sapiens.gene_info") ||  die "Cannot open Homo_sapiens.gene_info : $!\n";

open (PARSEDHUMAN,  ">hum_chr_loc_sym_mim.tab") || die "Can't open: hum_chr_loc_sym_mim.tab $!\n";


while (<HUMAN>) {
 chomp;
 
 $ctHumanLines++;
 next if $ctHumanLines < 2; 
 
 @fieldsHuman = split("\t");

 $taxonomyID = $fieldsHuman[0];
 ## make sure it is human being record
 next if $taxonomyID ne "9606";
 $geneId = $fieldsHuman[1];
 $Chr = $fieldsHuman[6];
 $loc = $fieldsHuman[7];

 ## Symbol_from_nomenclature_authority
 $symbol = $fieldsHuman[10]; 
 $dbXrefs = $fieldsHuman[5];
 
 ### assumption: MIM numbers are always 6 digits
 if ($dbXrefs =~ /MIM:(\d{6})/i) {
     $mim = $1;
 } else {
     $ctMissingMIM++;
     ### print "\n$geneId\t$Chr\t$loc\t$symbol\t$dbXrefs\n";
     $mim = " ";
 }
 
 ###print "\n$geneId\t$Chr\t$loc\t$symbol\t$mim\n" if $ctHumanLines < 10;
 
 print PARSEDHUMAN "$geneId\t$Chr\t$loc\t$symbol\t$mim\n";
 
 undef @fieldsHuman;
 
}
 
close (HUMAN);
close (PARSEDHUMAN);


system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> update_human_ortho_loc.sql >updateHumanOrthologyLog1 2> updateHumanOrthologyLog2");


print "\nHuman part tested........ctMissingMIM = $ctMissingMIM...........\n";

exit;


