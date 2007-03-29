#!/private/bin/perl
#
#  goparser.pl
#


open (INDEXFILE, "go.zfin") or die "open failed";
open (UNL, ">gene_association.zfin") or die "Cannot open exppat.unl";

print UNL "!Version: \$"."Revision\$ \n";
print UNL "!Date: \$"."Date\$ \n";
print UNL "!From: ZFIN (zfin.org) \n";
print UNL "! \n";

# set count to 0 before processing, increment it with each row processed.
$lastmrkrgoev = '';
@inf_array = ();
$db='ZFIN';

while ($line = <INDEXFILE>) {
      chomp $line;
      @fields = split /\t/, $line;
      $mrkrgoev=$fields[0];
      if ($lastmrkrgoev ne '' && $mrkrgoev ne $lastmrkrgoev) {

	  print UNL "$db\t$mrkrid\t$mrkrabb\t$qualifier\tGO:$goid\tZFIN:$pubid\t$evidence\t".join('|',@inf_array)."\t$go_o\t$mrkrname\t\tgene\ttaxon:7955\t$ev_date\t$mod_by\n";
	  
	  @inf_array = (); 
      }
      $lastmrkrgoev = $mrkrgoev;
      $mrkrid=$fields[1];
      $mrkrabb=$fields[2];
      $mrkrname=$fields[3];
      $qualifier=goQlf($fields[9]);
      $goid=$fields[4];
      $pubid=goPub($fields[5],$fields[6]);
      $evidence=$fields[7];
      $inf=goInf($fields[8]);
      push(@inf_array, $inf);
      $go_o=goAspect($fields[10]);
      $ev_date=goDate($fields[11]);
      $mod_by=goMod($fields[12]);

}

close (UNL);
close (INDEXFILE);

sub goQlf() 
 {
     $qualf = $_[0];
     $qualf = 'NOT' if $qualf eq 'not';
     $qualf = 'contributes_to' if $qualf eq 'contributes to';
     return $qualf;
 }              

sub goDate() 
  {
    ($date, $time) = split(/ /, $_[0]);
    $date =~ s/-//g;
    return $date;
  }
    
sub goAspect()
  {
    $aspect = $_[0];
    $aspect = 'P' if ($aspect eq 'B');
    $aspect = 'F' if ($aspect eq 'M');

    return $aspect;
  }

sub goPub()
  {
    $accession = $_[1];
    $pub =$_[0];
    $pmid='|PMID:';
    $pub = $pub.$pmid.$accession if (length($accession)!=0 && ($accession ne 'none'));
    $pub = $pub if (length($accession)==0);
    return $pub;
  }
sub goMod()
  {
    $mod_by =$_[0];
    $source = 'ZFIN' if ($mod_by ne 'S-P Curators');
    $source = 'UniProt' if ($mod_by eq 'S-P Curators');
    return $source;
  }
sub goInf()
  {
    $inf =$_[0];

    $inf =~ s/GenBank:/EMBL:/g;
    $inf =~ s/GenPept:/protein_id:/;
    $inf =~ s/RefSeq:NM_/NCBI_NM:NM_/;
    $inf =~ s/RefSeq:NP_/NCBI_NP:NP_/;

    if (index($inf,'\ ')==0) {
       $inf=~s/\\ //;
     }
    return $inf;
  }

