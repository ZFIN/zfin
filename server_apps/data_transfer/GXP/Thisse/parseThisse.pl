#!/private/bin/perl -w
#
# Convert csv files to pipe separated format, while do some
# data massage, such as trim off additional space and carriage
# returns, correct some possible typos, as well as
# translate stage code, and adjust expression comments.
#
# Read in :  probes.csv
#            expression.csv
#            images.csv
#            authors.csv
#
# Output  :  
#      probes.raw :   probe file which need furture massage
#      accession.unl  :   for FR#/EU# to ZFIN marker translation table
#      expression.unl
#      images.unl
#      authors.unl
# if Error :  
#      parseThisse.err 
#

#=============================
# subfunction trimString
#
# take off heading or trailing space, carriage return,
# and quotes
#
# parameter: string variable
# output   : trimed string variable
#

sub trimString ($) {
    my $field = shift;
    $field =~ s/^[\s\013\"]+//i;   # \013 eq ^K
    $field =~ s/[\s\013\"]+$//i;
    return $field;
}

#================================
# subfunction processCsvRow ($)
#
# separate row elements, trim each element,
# replace ^K with <br>
# 
# parameter: row string from .csv file
# output   : array with trimed string variable
#

sub processCsvRow ($) {
    my $csvrow = shift;
    my @row = split(/","/, $csvrow);
    for (my $i = 0; $i < @row; $i++) {
	$row[$i] = trimString ($row[$i]);
	$row[$i] =~ s/\013/<br>/g;
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
open ERR, ">parseThisse.err" or die "Cannot open parseThisse.err to write";

#####################
# PROBES
# 
#      0  _keyValue 
#      1  clone_name 
#      2  gene_zdb_id 
#      3  gb5p/refseq forward prime
#      4  gb3p/refseq reverse prime
#      5  library 
#      6  digest
#      7  vector 
#      8  pcr amplification
#      9  insert_kb
#      0  cloning_site
#      1  polymerase 
#      2  comments 
#      3  rating
#      4  modified_date 
#######################

open PROBE_IN, "<probes.csv" or die "Cannot open probes.csv file for read";
open PROBE_OUT, ">probes.raw" or die "Cannnot open probes.raw file for write";
open ACC, ">accession.unl" or die "Cannot open accession.unl file for write";

while (<PROBE_IN>) {
    
    my @probe = processCsvRow ($_); 

    $probe[1] =~ tr/[A-Z]/[a-z]/;       # EST name has to be lower in zfin

    $probe[8] =~ s/\222/\' /g;     #\222 is for windows single quote
    $probe[8] =~ s/3\'\s+5\'/3\'<br>5\'/;

    print ERR "$probe[1] misses insert_kb \n" unless $probe[9];    
    $probe[9] =~ tr /,/./;         #insert_kb  
    $probe[9] *= 1000;
 
    $probe[12] =~ s/<br>/ /g;      #remove line break in comments field, conservatively replace by space

    $probe[13] =~ s/--//g;         #clone rating could be null

    print PROBE_OUT join("|", @probe)."|\n";
    print ACC "$probe[1]|$probe[3]|\n";   
}
close (PROBE_IN);
close (PROBE_OUT);
close (ACC);

##########################
# EXPRESSION  
#
# see below for columns
##########################

open EXP_IN, "<expression.csv" or die "Cannot open expression.csv file for read";
open EXP_OUT, ">expression.unl" or die "Cannnot open expression.unl file for write";

while (<EXP_IN>) {
    
    # row columns
    my ($exp_keyValue,
	$exp_stage_code,
	$exp_description,
	$exp_level,
	$exp_keywords,
	$exp_modified_date) = processCsvRow ($_); 

    my $exp_stage_range = getStageRange($exp_stage_code);
    my $exp_found;

    # use blank for no comments.
    $exp_description = "" if ($exp_description =~ /no comment/i) ;	
	
    # the corresponding database field is defined as boolean type
    # we need to map the integer to boolean and save the level term
    # into description/comment field. 
    if ($exp_level == "0") {	
	$exp_description = "no expression".($exp_description ? "<br>".$exp_description : ""); 
	$exp_found = "f";
    }

    if ($exp_level == "1" ) {
	$exp_found = "t";
    }

    if ($exp_level == "2" ) {	
	$exp_description = "basal level of expression".($exp_description ? "<br>".$exp_description : ""); 
	$exp_found = "t";
    }

    if ($exp_level == "3" ) {	
	$exp_description = "not spatially restricted".($exp_description ? "<br>".$exp_description : ""); 
	$exp_found = "t";
    }
    # additional comments are day5 specific and thisse specific, so we deal
    # with it here intead of in the loading script when is a generic. 
    if ($exp_stage_code =~ /Larval:Day 5/i) {
	$exp_description .= "<br>" if  $exp_description;
	$exp_description .= "Please note that in 5 day old embryos some structures are not accessible to the probe (such as notochord, most of the trunk and tail). Therefore the description of the expression pattern is only partial.";
    }
	
    # anatomy term become part of the alternate key on expression_result
    # table. "NULL" has to be mapped to "unspecified" in case of level 1,
    # expressed, otherwise, "whole organism". 	
    if (! $exp_keywords) {
	$exp_keywords = ($exp_level==1) ? "unspecified" : "whole organism";
    }

    foreach my $eachkeyword (split(/<br>/, $exp_keywords)) {
	# some terms in the template are of formate previous_name(current_name)
	# e.g. optic nerve (cranial nerve II)
	if ($eachkeyword =~ /\((\s*.+\s*)\)/) {
	    $eachkeyword = $1;
	}
	
	print EXP_OUT join("|", $exp_keyValue, $exp_stage_range, $exp_description, $exp_found, $eachkeyword, $exp_modified_date)."|\n";
    }

}
close  EXP_IN;
close  EXP_OUT;


#######################
# IMAGES
# 
# see below for columns
########################

open IMG_IN, "<images.csv" or die "Cannot open images.csv file for read";
open IMG_OUT, ">images.unl" or die "Cannnot open images.unl file for write";

while (<IMG_IN>){

    # row columns
    my ($img_keyValue,
	$img_filename,
	$img_stage_code,
	$img_view,
	$img_direction,
	$img_specimen,
	$img_comments,
	$img_modified_date) = processCsvRow ($_); 
    
    $img_filename =~ s/.jpg//i;
    $img_view =~ s/side view \(lateral\)/side view/;
    my $img_stage_range = getStageRange($img_stage_code);

    print IMG_OUT join("|", $img_keyValue,$img_filename,$img_stage_range,$img_view,$img_direction,$img_specimen,$img_comments,$img_modified_date)."|\n";
}

close (IMG_IN);
close (IMG_OUT);

########################
# AUTHORS
#
# see below for columns
########################

open AUT_IN, "<authors.csv" or die "Cannot open authors.csv file for read";
open AUT_OUT, ">authors.unl" or die "Cannnot open authors.unl file for write";

while (<AUT_IN>){

    my @author = processCsvRow ($_); 
    print AUT_OUT join("|", @author)."|\n";
}

close (AUT_IN);
close (AUT_OUT);

close (ERR);

exit;
