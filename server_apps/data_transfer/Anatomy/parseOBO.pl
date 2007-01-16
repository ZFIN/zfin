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
#         cell_ids.unl: ZFC and CL id pairs
# 
use strict;

my ($termId, $termName,$termXref,$termCL, @mergedTerms, @termXrefs, $termStartStg, $termEndStg, @termPartOf,@termDevelopsFrom,@termIsA,$termDef, $termComment, @termSynonym);

&initiateVar ();

open ANATNEW, ">anatitem_new.unl" or die "Cannot open anatitem_new.unl file for write \n";
open ANATEXT, ">anatitem_exist.unl" or die "Cannot open anatitem_exist.unl file for write \n";
open ANATOBS, ">anatitem_obsolete.unl" or die "Cannot open anatitem_obsolete.unl file for write \n";
open ANATREL, ">anatrel.unl" or die "Cannot open anatrel.unl file for write \n";
open ANATALIAS, ">anatalias.unl" or die "Cannot open anatalias.unl file for write \n";
open ANATMERG, ">anatitem_merged.unl" or die "Cannot open anatmerge.unl file for write \n";

open CELLIDS, ">cell_ids.unl" or die "Cannot open cell_ids.unl file for write \n";

$/ = "\n\n[";
while (<>) {

    next unless /^Term/;                   #skip header and Typedef
    next if /id:\s+ZFA:0100000/i;  #skip place holders 
    next if /id:\s+ZFS:\d+/;               #skip stage terms
 
    #---------------------------------------------
    # Obsolete term
    # write out zfin id, filter out new terms that 
    # are not supposed to be flagged.
    #--------------------------------------------- 
    if (/is_obsolete: true/) { 
	if (/xref:\s+ZFIN:(\S+)/) {
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
       	if ( /^xref:\s+ZFIN:(\S+)/ ) {
	    push @termXrefs, $1; 
	    next;
	}
	if (/^xref:\s+(CL:\d+)/) {
	    $termCL = $1;
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
	    $termDef =~ s/\\n/ /g;   # replace '\n' to a space character
	    next;
	}
	if ( /^synonym:\s+\"(.+)\"/ ) {
	    push @termSynonym, &stringTrim($1);
	    push @termSynonym, /RELATED\s+PLURAL/ ? "plural" : "alias";
	    push @termSynonym, /\[ZFIN:(\S+)\]/ ? $1 : "";
	    next;
	}
	if ( /^comment:\s+(.+)/ ) {
	    $termComment = $1;
	    $termDef =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $termDef =~ s/\\n/ /g;   # replace '\n' to a space character
	    next;
	}

    } # end foreach term attribute processing
    
    # the xref of the merged term would became a xref line for the 
    # merged-into term, inserting before any existing xref line. Curators 
    # should only merge an existing term to either a brand new term or
    # another existing term. If to a brand new term, the xref line would
    # be ignored; if to an existing term, the last xrefline should be used.
    for (my $numOfMergs = @mergedTerms; $numOfMergs > 0; $numOfMergs--) {
	shift @termXrefs;
    }
    $termXref = shift @termXrefs;

    #-----------------------------------------------
    # Anatomy term Continue (non-obsolete)
    # verification work
    #----------------------------------------------- 

    # Warning if both start and end stages are undefined
    print "Both stages undefined for $termId\n" 
	unless ($termId ne "ZFS:0100000") || $termStartStg || $termEndStg;

    # otherwise replace undefined with the "Unknown"
    $termStartStg = "ZFS:0000000" unless $termId =~ /ZFS:0100000/ || $termStartStg;
    $termEndStg = "ZFS:0000000" unless $termId =~ /ZFS:0100000/ || $termEndStg;
 
    # Warning if both start and end stages are Unknown
    print "Both stages defined as Unknown for $termId\n" 
	if ($termId ne "ZFS:0100000") && ($termStartStg eq $termEndStg) && ($termStartStg eq "ZFS:0000000");

    # Warning if obo id is not exactly 7 digits
    print "OBO id not 7 digits: $termId\n" unless ($termId =~ /\d{7}/) && ($termId !~ /\d{8}/) ;

    #-----------------------------------------------
    # Anatomy term Continue (non-obsolete)
    # write information to different files for loading
    #-----------------------------------------------    
    if ( $termXref )  {

	print ANATEXT join("|", $termXref, $termName, $termStartStg, $termEndStg, $termDef, $termComment,"\n");
    }
    else {
	# the last column is saved for new zdb id
	print ANATNEW join("|", $termId, $termName, $termStartStg, $termEndStg, $termDef, $termComment)."||\n";
    }

    print CELLIDS  join("|", $termId, $termCL, "\n") if $termCL; 

    # the last column is saved for new zdb id
    foreach (@mergedTerms) {
	print ANATMERG join("|", $termId, $_)."|\n";
    }    
    # shift out synonym, type and attribution
    # the last column is saved for new zdb id
    while (@termSynonym) {
	print ANATALIAS join("|", $termId, shift @termSynonym, shift @termSynonym, shift @termSynonym )."||\n";
    }
    foreach (@termPartOf) {
	print ANATREL join("|", $_, $termId, "part_of")."|\n";
    }
    foreach (@termDevelopsFrom) {
	print ANATREL join("|", $_, $termId, "develops_from")."|\n";
    }
    foreach (@termIsA) {
	print ANATREL join("|", $_, $termId, "is_a")."|\n";
    }
    
    &initiateVar ();
    
} # end reading the input

close ANATNEW;
close ANATEXT;
close ANATOBS;
close ANATREL;
close ANATALIAS;
close ANATMERG;
close CELLIDS;

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
    $termCL = "";
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

