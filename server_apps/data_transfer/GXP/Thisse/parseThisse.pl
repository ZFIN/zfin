#!/private/bin/perl -w
#
# Read in :  probes.csv
#            expression.csv
#            images.csv
#            authors.csv
#
# Convert csv files to pipe separated format, while do some
# data massage, such as trim off additional space and carriage
# returns, correct some possible typo type mistakes, as well as
# translate stage code, and adjust expression comments.
#
# Output  :  probes.raw
#            frAcc.unl
#            expression.unl
#            keywords.unl
#            images.unl
#            authors.unl
# if error:  parseThisse.err 
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
# output   : trimed array variable
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
# subfunction editExpDescription ($$)
#
# adjust expression description according 
# to the type
#
# parameter: exp type
#            exp comment
# output:    exp comment with exp type
#
	    
sub editExpDescription ($$) {

    my $exptype  = shift;
    my $expdesc  = shift;

    if ($expdesc =~ /no comment/i) {
	$expdesc = $exptype;
	
    }else {
	$expdesc = $exptype."<br>".$expdesc;
    }
    
    return $expdesc;
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
    $stage{"ES"} = "Segmentation:1-somite|Segmentation:10-somite";
    $stage{"MS"} = "Segmentation:14-somite|Segmentation:14-somite";
    $stage{"24h"} = "Segmentation:20-somite|Pharyngula:Prim-5";
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

##############
# PROBES
# 
#      0  _keyValue 
#      1  clone_name 
#      2  gene_zdb_id 
#      3  gb5p 
#      4  gb3p 
#      5  library 
#      6  digest
#      7  vector 
#      8  pcr amplification
#      9  insert_kb
#      0  cloning_site
#      1  polymerase 
#      2  comments 
#      3  modified_date 
##############

open PROBE_IN, "<probes.csv" or die "Cannot open probes.csv file for read";
open PROBE_OUT, ">probes.raw" or die "Cannnot open probes.raw file for write";
open FR_ACC, ">frAcc.unl" or die "Cannot open frAcc.unl file for write";

while (<PROBE_IN>) {
    
    my @probe = processCsvRow ($_); 

    $probe[8] =~ s/\222/\' /g;     #\222 is for windows single quote
    $probe[8] =~ s/3\'\s+5\'/3\'<br>5\'/;
    
    $probe[9] =~ tr /,/./;         #insert_kb   
 
    $probe[12] =~ s/<br>/ /g;      #remove line break in comments field, conservatively replace by space
    print ERR "$probe[1] misses insert_kb \n" unless $probe[8];

    print PROBE_OUT join("|", @probe)."|\n";
	print FR_ACC "$probe[1]|$probe[3]|\n";   #for FR# - ZFIN gene translation table
}
close (PROBE_IN);
close (PROBE_OUT);
close (FR_ACC);

##################
# EXPRESSION  
#
##################

open EXP_IN, "<expression.csv" or die "Cannot open expression.csv file for read";
open EXP_OUT, ">expression.unl" or die "Cannnot open expression.unl file for write";
open KWD_OUT, ">keywords.unl" or die "Cannnot open keywords.unl file for write";

while (<EXP_IN>) {
    
    my ($exp_keyValue,
	$exp_stage_code,
	$exp_description,
	$exp_level,
	$exp_keywords,
	$exp_modified_date) = processCsvRow ($_); 

    my $exp_stage_range = getStageRange($exp_stage_code);
    my $exp_found;

    if ($exp_level == "0") {	
	$exp_description = editExpDescription("no expression", $exp_description); 
	$exp_found = "f";
    }

    if ($exp_level == "1" ) {
	$exp_found = "t";
    }

    if ($exp_level == "2" ) {	
	$exp_description = editExpDescription("basal level of expression", $exp_description); 
	$exp_found = "t";
    }

    if ($exp_level == "3" ) {	
	$exp_description = editExpDescription("not spatially restricted", $exp_description); 
	$exp_found = "t";
    }	

    print EXP_OUT join("|", $exp_keyValue, $exp_stage_range, $exp_description, $exp_found, $exp_keywords, $exp_modified_date)."|\n";

    ##################
    # KEYWORDS
    #
    #   _keyValue
    #   kwd_stage
    #   kwd_keyword
    #   kwd_modified_date
    #
    ####################

    foreach $kwd_keyword (split(/<br>/, $exp_keywords)) {
	print KWD_OUT join("|", $exp_keyValue, $exp_stage_range, $kwd_keyword,$exp_modified_date)."|\n";
    }
    
}
close  EXP_IN;
close  EXP_OUT;
close  KWD_OUT;


#################
# IMAGES
# 
# 
################

open IMG_IN, "<images.csv" or die "Cannot open images.csv file for read";
open IMG_OUT, ">images.unl" or die "Cannnot open images.unl file for write";

while (<IMG_IN>){

    my ($img_keyValue,
	$img_filename,
	$img_stage_code,
	$img_view,
	$img_direction,
	$img_specimen,
	$img_comments,
	$img_modified_date) = processCsvRow ($_); 
    
    $img_filename =~ s/.jpg//;
    $img_view =~ s/side view \(lateral\)/side view/;
    my $img_stage_range = getStageRange($img_stage_code);

    print IMG_OUT join("|", $img_keyValue,$img_filename,$img_stage_range,$img_view,$img_direction,$img_specimen,$img_comments,$img_modified_date)."|\n";
}

close (IMG_IN);
close (IMG_OUT);

#################
# AUTHORS
#
#
#################

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
