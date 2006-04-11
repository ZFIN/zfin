#!/local/bin/perl

# FILE: parseGOobo.pl
# PREFIX: none

# DESCRIPTION: 
# INPUT: OBO format gene ontology file
# OUTPUT: error message to STDOUT
#         goterm_obsolete.unl - obsolete terms in gene_ontology.obo
#         goterm_parsed.unl - parsed obo file.
#         goterm_secondary.unl - primary terms with their 
#                                respective secondary terms in 
#                                gene_ontology.obo
#         gotermonto.unl - copy of the parsed gene_ontology.obo file
#         new_obsolete_terms.unl - new (to ZFIN) obsolete terms.
#         newannotsecterms.unl - new (to ZFIN) secondary terms with annotations
#         newsecterms.unl - all secondary terms.
#         newterms.unl - new (to ZFIN) goterms
#         obso_sec_with.unl - annotations with obsolete or secondary
#                             terms in their 'inferred from/with' field
#         reinstated_go_terms.txt - terms that have been reinstated (made
#                                   non-obsolete or non-secondary).
#         updatedterms.unl - terms that have same ids, but different names.
#         report.txt - sql output of loadgoterms.sql
#         report_not_secondary_any_more.unl - reinstated secondary terms.

# set environment variables

$ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
$ENV{"INFORMIXSERVER"}="wanda";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";

# make sure in the right directory

$dir = "/research/zcentral/www_homes/hoover/server_apps/data_transfer/LoadGO/";
chdir "$dir";
print "$dir"."\n" ;

# SubRoutines #

#=====================================
# sub initiateVar
# 
# reset the variables to null
#

sub initiateVar  {
    $termId = "";
    $termName = "";
    $namespace = "";
    $definition = "";
    @is_a = ();
    @alt_id = ();
    $xref_analog = "";
    @relationship = ();
    $comment = "";
    $is_obsolete= "";
    $xref_unknown = "";
    @subset = ();
    @exact_synonym = ();
    @narrow_synonym = ();
    @related_synonym = ();
    @broad_synonym = ();
    $is_transitive = "" ;
    @termContent = () ;
    @mergedTerms = ();
    $term = "";
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

my ($termId, $termName,$namespace,$definition, @is_a, @alt_id, $xref_analog, @relationship, $comment, $is_obsolete,$xref_unknown,@subset, @exact_synonym,@narrow_synonym, @related_synonym,@broad_synonym,$is_transitive, @termContent, @mergedTerms, $term, $pipe_newline);

$pipe_newline = "|\n" ;

# initiate variables to null

&initiateVar ();

# open output files

open GOOBS, ">goterm_obsolete.unl" or die "Cannot open goterm_obsolete.unl file for write \n";
open PARSED, ">goterm_parsed.unl" or die "Cannot open goterm_parsed.unl file for write \n";
open SECONDARY, ">goterm_secondary.unl" or die "Cannot open goterm_secondary.unl file for write \n";

# set the record seperator equal to "two new lines and a [" so that an entire Go term
# block will be read on each pass of the while loop.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to "two new lines and a ["

$/ = "\n\n[";

# access the ARGV handle; aka: read one line of the file at a time

while (<>) {

    next unless /^Term/;                   #skip header and Typedef

    # now split the rest of the term block on the newline
    # and stick each line into its appropriate variable.

    my @termContent = split /\n/;
 
   foreach (@termContent) {

	if ( /^id:\s+(\S+)/ ) {
	    
	    # assign the id value to the $termId variable.
	    $termId = $1; 
	    
	    # go to the next line inside the term block
	    next;
	}	

	if ( /^name:\s+(.+)/ ) {
	    $termName = $1; 
	    next;
	}

	if ( /^alt_id:\s+(\S+)/ ) {

	    # push the alt_id into the @mergedTerms array since
	    # there can be more than one alt_id per term.

	    push @mergedTerms, $1; 

	    next;
	}

	if ( /^namespace:\s+(.+)/ ) {
	    $namespace = $1; 
	    next;	    
	}
	if ( /^is_obsolete: true/ ) {

	    $is_obsolete = 'true' ;
	    
	    # push obsolete terms to the parsed file, and to a file 
	    # GOOBS, that only has obsolete terms.

	    print GOOBS $termId."||\n" ;
	    next;
	}
	
	if ( /^def:\s+\"(.+)\"/ ) {
	    $definition = &stringTrim($1);
	    $definition =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $definition =~ s/\\n/ /g;   # replace '\n' to a space character
	    next;
	}

	if ( /^comment:\s+\"(.+)\"/ ) {
	    $comment = &stringTrim($1);
	    $comment =~ s/\'/\"/g;   # double quotes are preferred in table storage
	    $comment =~ s/\\n/ /g;   # replace '\n' to a space character
	    next;
	}

    } # end foreach term attribute processing

    # output the parsed term into a line in the PARSED file

    print PARSED join("|", $termId, $termName, $namespace, $definition, $comment, $is_obsolete).$pipe_newline;

    # loop thru the parsed secondary/alt_ids and put them in a file
    # with their now primary ids.

    foreach $term (@mergedTerms) {
	print SECONDARY join("|", $termId, $term).$pipe_newline;
	
    }

    # set mergedTerms, is_obsolete, definition, and comment variables to null so that 
    # terms without secondary/alt_ids terms won't pick
    # up the secondary terms from the previous round of parsing.

    @mergedTerms =() ;
    $is_obsolete = "" ;
    $definition = "" ;
    $comment = "" ;

} # end MAIN
