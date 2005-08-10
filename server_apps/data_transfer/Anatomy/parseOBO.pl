#!/private/bin/perl -w
#
# The script extracts information from OBO format anatomy
# file, and writes them to unload files to update anatomy_item and 
# anatomy_relationship tables. 
#
# INPUT: OBO format anatomy ontology file
#
# OUTPUT: error message to STDOUT
#         anatitem_new.unl: term id starts with AO
#         anatitem_exist.unl: term id contains ANAT
#         anatitem_obsolete.unl: term has is_obsolete:true
#         anatitem_merged.unl: term has alt_id:xxxxx
#         anatalias.unl: term and each of its synonym
#         anatrel.unl: parentTerm|childTerm|relType
#         stage_ids.unl: ZFS and ZDB-STAGE- id pairs
#         anatitem_ids.unl: ZFA and ZDB-ANAT- id pairs
# 
use strict;

my ($termId, $termName,$termXref, @mergedTerms,$termStartStg, $termEndStg, @termPartOf,@termDevelopsFrom,@termIsA,$termDef, $termComment, @termSynonym);

&initiateVar ();

open ANATNEW, ">anatitem_new.unl" or die "Cannot open anatitem_new.unl file for write \n";
open ANATEXT, ">anatitem_exist.unl" or die "Cannot open anatitem_exist.unl file for write \n";
open ANATOBS, ">anatitem_obsolete.unl" or die "Cannot open anatitem_obsolete.unl file for write \n";
open ANATREL, ">anatrel.unl" or die "Cannot open anatrel.unl file for write \n";
open ANATALIAS, ">anatalias.unl" or die "Cannot open anatalias.unl file for write \n";
open ANATMERG, ">anatitem_merged.unl" or die "Cannot open anatmerge.unl file for write \n";

open STAGEIDS, ">stage_ids.unl" or die "Cannot open stage_ids.unl file for write \n";
open ANATIDS, ">anatitem_ids.unl" or die "Cannot open anatitem_ids.unl file for write \n";

$/ = "\n\n[";
while (<>) {

    next unless /^Term/;                   #skip header and Typedef
    next if /name:\s+Zebrafish Anatomy/i;  #skip place holders 

    #-------------------------------------------
    # Stage term
    # write out obo id and zfin id pair for term
    # start stage and end stage id translation
    #---------------------------------------------
    if (/id:\s+(ZFS:\d+)/ ) {    
	my $stageId = $1;
	/xref_analog:\s+ZFIN:(\S+)/;
	print STAGEIDS join("|", $stageId, $1, "\n");
	next;
    }

    #---------------------------------------------
    # Obsolete term
    # write out zfin id, filter out new terms that 
    # are not supposed to be flagged.
    #--------------------------------------------- 
    if (/is_obsolete: true/) { 
	if (/xref_analog:\s+ZFIN:(\S+)/) {
	    print ANATOBS join("|", $1, "\n");
	}
	next;
    }

    #---------------------------------------------
    # Anatomy term (non-obsolete)
    # split and process each attribute line
    #--------------------------------------------- 
    
    my @termContent = split /\n/;
    foreach (@termContent) {

	if ( /^id:\s+(\S+)/ ) {
	    $termId = $1; 
	    next;
	}	
	if ( /^name:\s+(.+)/ ) {
	    $termName = $1; 
	    next;
	}
       	if ( /^xref_analog:\s+ZFIN:(\S+)/ ) {
	    $termXref = $1; 
	    next;
	}
	if ( /^alt_id:\s+(\S+)/ ) {
	    push @mergedTerms, $1; 
	    next;
	}
	if ( /^relationship:\s+(\S+)\s+(\S+)/ ) {
	    $termStartStg = $2 if ($1 eq "start");
	    $termEndStg = $2 if ($1 eq "end");
	    push @termPartOf, $2 if ($1 eq "part_of");
	    push @termDevelopsFrom, $2 if ($1 eq "develops_from");
	    next;
	}
	if ( /^is_a:\s+(\S+)/ ) {
	    my $parent = $1;

	    ### if parent is not place holder
	    push @termIsA, $parent if ($parent ne "ZFA:0100000" );

	    next;
	}
	# ignore the dbxref for now
	if ( /^def:\s+\"(.+)\"/ ) {
	    $termDef = &stringTrim($1);
	    $termDef =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $termDef =~ s/\\n/ /g;   # replace '\n' to a space character
	    next;
	}
	if ( /^synonym|related_synonym:\s+\"(.+)\"/ ) {
	    push @termSynonym, &stringTrim($1);
	    next;
	}
	if ( /^comment:\s+(.+)/ ) {
	    $termComment = $1;
	    $termDef =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $termDef =~ s/\\n/ /g;   # replace '\n' to a space character
	    next;
	}
    } # end foreach term attribute processing

    #-----------------------------------------------
    # Anatomy term Continue (non-obsolete)
    # write out info to different files for loading
    #----------------------------------------------- 

    # if both start and end stages are undefined, output for warning
    print "Both stages undefined for $termId\n" 
	unless $termId =~ /ZFS:0100000/ || $termStartStg || $termEndStg;

    # otherwise replace undefined with the "Unknown"
    $termStartStg = "ZFS:0000000" unless $termId =~ /ZFS:0100000/ || $termStartStg;
    $termEndStg = "ZFS:0000000" unless $termId =~ /ZFS:0100000/ || $termEndStg;
 
    print ANATIDS  join("|", $termId, $termXref,"\n");

    if ( $termXref )  {
	print ANATEXT join("|", $termXref, $termName, $termStartStg, $termEndStg, $termDef, $termComment,"\n");
    }else {
	# the last column is saved for new zdb id
	print ANATNEW join("|", $termId, $termName, $termStartStg, $termEndStg, $termDef, $termComment)."||\n";
    }

    # the last column is saved for new zdb id
    foreach (@mergedTerms) {
	print ANATMERG join("|", $termXref, $_)."|\n";
    }
    foreach (@termSynonym) {
	print ANATALIAS join("|", $termXref, $_)."||\n";
    }
    foreach (@termPartOf) {
	print ANATREL join("|", $_, $termXref, "part_of")."|\n";
    }
    foreach (@termDevelopsFrom) {
	print ANATREL join("|", $_, $termXref, "develops_from")."|\n";
    }
    foreach (@termIsA) {
	print ANATREL join("|", $_, $termXref, "is_a")."|\n";
    }
    
    &initiateVar ();
    
} # end reading the input

close ANATNEW;
close ANATEXT;
close ANATOBS;
close ANATREL;
close ANATALIAS;
close ANATMERG;
close STAGEIDS;
close ANATIDS;

exit;

#==================================
# sub initiateVar
#
# Initiate global variables
#
# Input:  None
# Return: None
#
# Effects: 
#         global variables initiated
# 
sub initiateVar  {
    $termId = "";
    $termName = "";
    $termStartStg = "";
    $termEndStg = "";
    @termPartOf = ();
    @termDevelopsFrom = ();
    @termIsA = ();
    @termSynonym = ();
    @mergedTerms = ();
    $termDef = "";
    $termComment = "";
    $termXref = "";
}
#=====================================
# sub stringTrim
#
# Trim the leading and trailing space
#
# Input:      string
# Return:      string
#

sub stringTrim ($) {
    my $inputStr = $_[0];

    $inputStr =~ s/^\s+//;
    $inputStr =~ s/\s+$//;

    return $inputStr;
}

