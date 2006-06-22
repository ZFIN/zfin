#!/private/bin/perl

# FILE: sortSNPs.pl
# fully parse the SNP data and prepare datafile for curator manual validation 
# input files: SubSNP.bcp, SubSNPAcc.bcp, snpUnsorted
# output files: snp.unl, snpGene.unl (for curator validation)

open (ACC1, "SubSNP.bcp") || die "Can't open SubSNP.bcp : $!\n";
@lines=<ACC1>;
close(ACC1);
%corrspZSNPs = %corrsps = %ZSNPs = (); 
$ct = 0;
foreach $line (@lines) {
  if ($line) {
    undef (@fields);
    @fields = split(/\s+/, $line); 
    $corrsp = $fields[0];
    $corrsps{$corrsp} = 1;
    $ZSNP = $fields[2];
    $ZSNPs{$ZSNP} = 1;
    $corrspZSNPs{$corrsp} = $ZSNP;
    $ct++;
  }
}
print "\nTotal of $ct entries from SubSNP.bcp\n";

open (ACC2, "SubSNPAcc.bcp") || die "Can't open SubSNPAcc.bcp : $!\n";
@lines=<ACC2>;
close(ACC2);
open (SNPNOTEST,  ">snpNotEST.txt") || die "Can't open: snpNotEST.txt $!\n";
$ct = 0;
%ZSNPaccs = %ZSNPmarkers = %corrsps2 = %accs = ();
foreach $line (@lines) {
  if ($line) {
    undef (@fields);
    @fields = split(/\s+/, $line); 
    $corrsp = $fields[0];
    $corrsps2{$corrsp} = 1;
    $accnum = $fields[2];
    $accs{$accnum} = 1;
    if (exists($corrspZSNPs{$corrsp})) {
      $ZSNP = $corrspZSNPs{$corrsp};
      if ($accnum && $accnum ne "UNKNOWNACC") {
        $ZSNPaccs{$ZSNP} = $accnum;
        $ct++;
      } 
    } 
  }
}
print "\nTotal of $ct entries from SubSNPAcc.bcp\n";

open (SNP, "snpUnsorted") || die "Can't open snpUnsorted : $!\n";
open (OUTPUT,  ">snp.unl") || die "Can't open: snp.unl $!\n";
open (SNPGENE,  ">snpGene.unl") || die "Can't open: snpGene.unl $!\n";
@lines=<SNP>;
close(SNP);
$ct = 0;
foreach $line (@lines) {
  $ct++;
  undef (@fields);
  @fields = split(/\s+/, $line);
  if (exists($ZSNPaccs{$fields[1]})) {
    $acc = $ZSNPaccs{$fields[1]};
  } else {
    $acc = -1;
  }
  print OUTPUT "$fields[0]|$fields[1]|$acc|$fields[2]|$fields[3]|$fields[4]\n";
  
  if ($acc != -1 && length $acc < 6) {
    $lcacc = lc $acc;
    print SNPGENE "$fields[0]|$fields[1]|$lcacc|$fields[2]|$fields[3]|$fields[4]\n";
  }
}

close(OUTPUT);
close(SNPGENE);

print "\n$ct of rows of data processed\n";

print "\nParsing dbSNP data is done.\n";