#!/private/bin/perl -w
#
# FILE: parse_stage_keywords.pl
#
# Convert .csv file from Thisse template file into
# .unl file to be used by sql file.
#
# INPUT:
#       stageKeyword.csv
# OUTPUT:
#       stageKeyword.unl
#
#       
use strict;

#================================
# subfunction processCsvRow ($)
#
# separate row elements, trim each element,
# replace ^K with <br>
# 
# parameter: row string from .csv file
# output   : trimed array variable
#

sub processCsvRow ($) {
    my $csvrow = shift;
    my @row = split(/","/, $csvrow);
    for (my $i = 0; $i < @row; $i++) {
	$row[$i] =~ s/^[\s\013\"]+//i;
	$row[$i] =~ s/[\s\013\"]+$//i;
    }
    return @row;
}

#================================
# subfunction getStageRange ($)
#
# translate stage code to range 
# or extend the stage to range by duplication
#
# parameter: stage code or name
# output:    stage range
#
sub getStageRange ($) {
    my $stagecode = shift;

    my %stage;
    $stage{"B"} = "Blastula:Sphere|Blastula:30%-epiboly";
    $stage{"G"} = "Gastrula:50%-epiboly|Gastrula:Bud";
    $stage{"ES"} = "Segmentation:1-4 somites|Segmentation:10-13 somites";
    $stage{"MS"} = "Segmentation:14-19 somites|Segmentation:14-19 somites";
    $stage{"24h"} = "Segmentation:20-25 somites|Pharyngula:Prim-5";
    $stage{"36h"} = "Pharyngula:Prim-15|Pharyngula:Prim-25";
    $stage{"48h"} = "Pharyngula:High-pec|Hatching:Long-pec";
    $stage{"all stages"} = "Zygote:1-cell|Hatching:Pec-fin";
    
    if ( $stagecode eq "B" ||
	 $stagecode eq "G" ||
	 $stagecode eq "ES" ||
	 $stagecode eq "MS" ||
	 $stagecode eq "24h" ||
	 $stagecode eq "36h" ||
	 $stagecode eq "48h" ||
	 $stagecode eq "all stages") {
	
	return ($stage{$stagecode});
    }
    else {
	return ("$stagecode|$stagecode");
    }
}

#================================================
# main 
#

open IN, "<stageKeyword.csv" or die "Cannot open file for read";
open OUT, ">stageKeyword.unl" or die "Cannnot open  file for write";

while (<IN>) {

   my ($stage_code, $keyword) = processCsvRow ($_); 
   my $stage_range = getStageRange($stage_code);

   # some terms in the template are of formate previous_name(current_name)
   # e.g. optic nerve (cranial nerve II)
   if ($keyword =~ /\((.+)\)/) {
       $keyword = $1;
   }
   print OUT join("|", $stage_range, $keyword)."||||\n";
}
close IN;
close OUT;

