#!/private/bin/perl

# set environment variables

$ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
$ENV{"INFORMIXSERVER"}="wanda";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";

my $fileName = $ARGV[0];
# make sure in the right directory

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/";
chdir "$dir";
print "parseObo.pl running in: $dir"."\n" ;


# SubRoutines #

#=====================================
# sub initiateVar
# 
# reset the variables to null
#

sub initiateVar  {
    $scopepile="";
    $syntype="";
    @synscopes=();
    $syn="";
    $scope="";
    @scopedef=();
    $termId = "";
    $id="";
    $db="";
    @refparts=();
    $is_anonymous="";
    $termName = "";
    $namespace = "";
    $definition = "";
    @is_a = ();
    @alt_id = ();
    @relationship = ();
    $comment = "";
    $is_obsolete= "";
    @subset = ();
    @synonym= ();
    @xref=();
    @intersection_of=();
    @union_of=();
    @disjoint_from=();
    @replaced_by=();
    @consider=();
    $is_transitive = "" ;
    @termContent = () ;
    @mergedTerms = ();
    $term = "";
    $relType="";
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

# MAIN # 

use strict;

# declare variables 

my ($id, $db,@refparts,$termId, $is_anonymous, @synonym, @xref, @intersection_of, @union_of, @disjoint_from,@replaced_by, @consider, $is_transitive, $termName, $namespace, $definition, @termPartOf, @termIsA, @alt_id, @relationship, $comment, $is_obsolete, @subset, $is_transitive, @termContent, @mergedTerms, $term, $pipe_newline,@synscopes,$syn,$scope,@scopedef,$scopepile,$syntype,$relType);

$pipe_newline = "|\n" ;

# initiate variables to null

&initiateVar ();

system("/private/ZfinLinks/Commons/bin/reline $fileName");
system("./parseHeader.pl $fileName");

print "starting term parsing\n";
# open output files

open OBS, ">term_obsolete.unl" or die "Cannot open term_obsolete.unl file for write \n";
open PARSED, ">term_parsed.unl" or die "Cannot open term_parsed.unl file for write \n";
open TYPEDEF, ">term_typedef.unl" or die "Cannot open term_typedef.unl file for write \n";
open SECONDARY, ">term_secondary.unl" or die "Cannot open term_secondary.unl file for write \n";
open REL, ">term_relationships.unl" or die "Cannot open term_relationships.unl file for write \n";
open REPLACED, ">term_replaced.unl" or die "Cannot open term_replaced.unl file for write \n";
open SYN, ">term_synonyms.unl" or die "Cannot open term_synonym.unl file for write \n";
open INTERSECTION, ">term_intersections.unl" or die "Cannot open term_intersections.unl file for write \n";
open UNION, ">term_unions.unl" or die "Cannot open term_unions.unl file for write \n";
open CONSIDER, ">term_consider.unl" or die "Cannot open term_consider.unl file for write \n";
open DISJOINT, ">term_disjoint.unl" or die "Cannot open term_disjoint.unl file for write \n";
open XREF, ">term_xref.unl" or die "Cannot open term_xref.unl file for write \n";
open SUBSET, ">term_subset.unl" or die "Cannot open term_subset.unl file for write \n";

# set the record seperator equal to "two new lines and a [" so that an entire  term
# block will be read on each pass of the while loop.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to "two new lines and a ["

$/ = "\n\n[";

# access the ARGV handle; aka: read one line of the file at a time

while (<>) {

    next unless (/^Term/ or /^Typedef/);                   #skip header and Typedef

    # now split the rest of the term block on the newline
    # and stick each line into its appropriate variable.
    
    my @termContent = split /\n/;
    
    foreach (@termContent) {
	
	if ( /^id:\s+(\S+)/ ) {
	    
	    # assign the id value to the $termId variable.
	    $termId = $1; 
	    
	    # print "$termId\n" ;
	    #  to the next line inside the term block
	    next;
	}
	if ( /^name:\s+(.+)/ ) {
	    $termName = $1; 
	    next;
	}
	if ( /^is_anonymous:\s+(.+)/ ) {
	    $is_anonymous = $1; 
	    next;
	}    
	if ( /^alt_id:\s+(\S+)/ ) {
	    
	    push @mergedTerms, $1; 
	    
	    next;
	}
	if ( /^subset:\s+(\S+)/ ) {
	    
	    push @subset, $1; 
	    
	    next;
	}
	if ( /^intersection_of:\s+(\S+)/ ) {
	    
	    push @intersection_of, $1; 
	    
	    next;
	}
	if ( /^is_transitive:\s+(.+)/ ) {
	    $is_transitive = $1; 
	    next;	    
	}
	if ( /^union_of:\s+(\S+)/ ) {
	    
	    push @union_of, $1; 
	    
	    next;
	}
	if ( /^disjoint_from:\s+(\S+)/ ) {
	    
	    push @disjoint_from, $1; 
	    
	    next;
	}
	if ( /^consider:\s+(\S+)/ ) {
	    push @consider, $1; 
	    
	    next;
	    }
	if ( /^synonym:\s+(.+)/ ) {
	    push @synonym, stringTrim($1); 
	    
	    next;
	}
	if ( /^namespace:\s+(.+)/ ) {
	    $namespace = $1; 
	    next;	    
	}
	if ( /^replaced_by:\s+(.+)/ ) {
	    push @replaced_by, $1; 
		
	    next;	    
	}
	if ( /^is_obsolete: true/ ) {
	    
	    $is_obsolete = 't' ;
	    
	    # push obsolete terms to the parsed file, and to a file 
	    # OBS, that only has obsolete terms.
	    
	    print OBS $termId."|\n" ;
	    next;
	}
	if ( /^relationship:\s+(\S+)\s+(\S+)/ ) {
	    if ($1 eq "part_of") {
		push @termPartOf, $2;
	    }
	    else {
		$relType=$1;
		print REL join("|", $2, $termId, $relType)."|\n";
	    }
	    next;
	}
	if ( /^is_a:\s+(\S+)/ ) {
	    my $parent = $1;
	    
	    ### if parent is not place holder
	    push @termIsA, $parent ;
	    
	    next;
	}
	if ( /^xref:\s+(\S+)/ ) {
	    
	    push @xref, $1; 
	    
	    next;
	}
	# ignore the dbxref for now
	if ( /^def:\s+\"(.+)\"/ ) {
	    $definition = &stringTrim($1);
	    $definition =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $definition =~ s/\\n/ /g;   # replace '\n' to a space character
	    $definition =~ s/\|/\,/g;   # replace pipes with a comma for now--mainly in SO load.
	    next;
	}
	if ( /^comment:\s+(.+)/) {
	    $comment = &stringTrim($1);
	    $comment =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $comment =~ s/\\n/ /g;   # replace '\n' to a space character
	    #print $comment;
	    next;
	}
	
	
    } # end foreach term attribute processing
    
    
    # output the parsed term into a line in the PARSED file
    
    if ($termId =~ /.*\:.*/){
	print PARSED join("|", $termId, $termName, $namespace, $definition, $comment, $is_obsolete).$pipe_newline;
    }
    # loop thru the parsed secondary/alt_ids and put them in a file
    # with their now primary ids.
	
    foreach $term (@mergedTerms) {
	print SECONDARY join("|", $termId, $term).$pipe_newline;	
    }
    foreach (@termPartOf) {
	print REL join("|", $_, $termId, "part_of")."|\n";
    }
    foreach (@termIsA) {
	    print REL join("|", $_, $termId, "is_a")."|\n";
    }
    foreach (@replaced_by) {
	print REPLACED join("|", $_, $termId, "replaced_by")."|\n";
    }
    foreach (@synonym) {
	# !!! START HERE !!!
	# need to pull out the EXACT
	    
#synonym: "mitochondrial inheritance" EXACT []
	
	
	@synscopes = split(/"/);
	$syn = stringTrim(@synscopes[1]);
	
#synonym: "blood vessel formation from pre-existing blood vessels" EXACT systematic_synonym []
	    
	$scopepile = @synscopes[2];
	
	@scopedef = split(/\s+/,$scopepile);
	
	$scope = @scopedef[1];
	
	$syntype = @scopedef[2];
	
#GO:0001525|blood vessel formation from pre-existing blood vessels|EXACT|systematic_synonym|synonym|
#GO:0001527|extended fibrils|EXACT|[]|synonym|
	
	print SYN join("|", $termId, $syn,$scope,$syntype, "synonym")."|\n";
    }
    foreach (@intersection_of) {
	print INTERSECTION join("|", $termId, $_, "intersection_of")."|\n";
    }
    foreach (@union_of) {
	print UNION join("|", $termId, $_, "union_of")."|\n";
	}
    foreach (@consider) {
	print CONSIDER join("|", $termId, $_, "consider")."|\n";
    }
    foreach (@xref) {
	@refparts = split (/:/) ;
	foreach (@refparts){
	    $db =@refparts[0];
	    #print @refparts[0]."\n";
	    #print @refparts[1]."\n";
	    $id =@refparts[1];
	    $id =~ s/\\//;
	    #print $id."\n";
	}
	print XREF join("|", $termId, $db, $id, "xref")."|\n";
	$id="";
	$db="";
	@refparts=();
    }
	foreach (@subset) {
	    print SUBSET join("|", $termId, $_, "subset")."|\n";
	}
    
    # set mergedTerms, is_obsolete, definition, and comment variables to 
    # null so that 
    # terms without secondary/alt_ids terms won't pick
    # up the secondary terms from the previous round of parsing.
    
    @mergedTerms =() ;
    $is_obsolete = "" ;
    $definition = "" ;
    $comment = "" ;
    @termIsA = ();
    @termPartOf = ();
    @union_of = ();
    @intersection_of=();
    @consider=();
    @synonym=();
    @xref=();
    @subset=();
    $is_transitive="";
    @synscopes=();
    $syn="";
    $scope="";
    @scopedef=();
    $scopepile="";
    $syntype="";
    @replaced_by=();
    $relType="";
}

# end MAIN
print "finished parsing"."\n";
