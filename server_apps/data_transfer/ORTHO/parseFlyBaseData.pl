#!/private/bin/perl

########################################################################################
#  File: parseFlyBaseData.pl
#  This script parses the FlyBase data file to extract fruit fly chromosome info and
#  then appends the parsed result to "chromInfo.unl" for updating orthologue table
########################################################################################

open (INP, "flyOrthoData.unl") || die "Can't open flyOrthoData.unl : $!\n";
@lines=<INP>;

%abbrIds = %abbrs = ();
foreach $line (@lines) {
  @fields = split(/\|/, $line);
  $fly = $fields[1]; 
  $abbr = $fields[2]; 
  $id = $fields[3];
  if($fly eq "Fly" && $id) {
    $abbrIds{$id} = $abbr;
    $abbrs{$abbr} = 1;
  }
}

# open the file for appending the output text
open (RESULT,  ">>chromInfo.unl") || die "Can't open: chromInfo.unl $!\n";

$/ = ">";
open (INP2, "FlyBase.data") || die "Can't open FlyBase.data : $!\n";
@genes=<INP2>;

$ct = 0; @markers = @chrs = ();
foreach $gene (@genes) {
  $ct++; next if ($ct == 1);
  @fields = split(/db_xref=FlyBase:/, $gene);
  $idsWithSeq = $fields[1];
  @fields2 = split(/;/, $idsWithSeq);
  $idsStr = $fields2[0];

  @fields3 = split(/loc=/, $gene);
  $loc = $fields3[1]; 
  @fields4 = split(/:/, $loc);
  $chrom = $fields4[0];
  
  @fields5 = split(/name=/, $gene);
  $nameInfo = $fields5[1]; 
  @fields6 = split(/;/, $nameInfo);
  $name = $fields6[0];  

  if ($chrom) {
    if ($name && $abbrs{$name} == 1) {
      print RESULT "$name|$chrom||Fly";
      print RESULT "\n";  
    } else {
      @ids = split(/,/, $idsStr);
      foreach $id (@ids) {
        @idFields = split(/FlyBase:/, $id);
        next if ($#idFields != 1);
        $FlyBaseID = $idFields[1];
        if($FlyBaseID && exists($abbrIds{$FlyBaseID})) {
          $abbr = $abbrIds{$FlyBaseID};
          print RESULT "$abbr|$chrom||Fly";
          print RESULT "\n";
          last;
        }
      }
    }
  }
}
close INP;
close INP2;
close RESULT;

exit;
