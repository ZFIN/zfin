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
print "parseHeader.pl running in: $dir"."\n" ;

sub initiateVar  {
    $format_version = "";
    $data_version="";
    $date="";
    $saved_by = "";
    $auto_generated_by="";
    @subset_def=();
    @synonym_type_def=();
    $default_namespace="";
    $remark="";
    @defs=();
    $subset="";
    @typedefs=();
    $type="";
    $def="";
    $typedef="";
    $scope="";
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
my ($format_version,$data_version,$date,$saved_by,$auto_generated_by,@subset_def,@synonym_type_def,$default_namespace,$remark,@defs,$subset,@typedefs,$type,$def,$typedef,$scope);

my $pipe_newline = "|\n" ;

# initiate variables to null

&initiateVar ();

# open output files

open HEADER, ">ontology_header.unl" or die "Cannot open ontology_header.unl file for write \n";

open SUBSETDEFS, ">subsetdefs_header.unl" or die "Cannot open subsetdefs_header.unl file for write \n";
open SYNTYPEDEFS, ">syntypedefs_header.unl" or die "Cannot open syntypedefs_header.unl file for write \n";

while (<>) {
    
    if (/^Term/){
	print "Header Parsed";
	last; 
    }
    my @headerLine = split /:/;
    if (@headerLine[0] eq "format-version"){
	$format_version = stringTrim(@headerLine[1]);
	
    }
    if (@headerLine[0] eq "data-version"){
	$data_version = stringTrim(@headerLine[1]);

    }
   #date is f'd because in : format.

    if (@headerLine[0] eq "date"){
	$date = stringTrim(@headerLine[1]);

    }
    if (@headerLine[0] eq "saved-by"){
	$saved_by = stringTrim(@headerLine[1]);

    }
    if (@headerLine[0] eq "auto-generated-by"){
	$auto_generated_by = stringTrim(@headerLine[1]);

    }
    if (@headerLine[0] eq "subsetdef"){

	push @subset_def, stringTrim(@headerLine[1]); 

    }
    if (@headerLine[0] eq "synonymtypedef"){

	push @synonym_type_def, stringTrim(@headerLine[1]); 

    }
    if (@headerLine[0] eq "default-namespace"){
	$default_namespace = stringTrim(@headerLine[1]);

    }
    if (@headerLine[0] eq "remark"){
	$remark = stringTrim(@headerLine[1]);

    }
}
foreach (@subset_def) {
    @defs = split /"/ ;
    foreach (@defs){
	$subset = stringTrim(@defs[0]);
	$def = stringTrim(@defs[1]);	
    }
    print SUBSETDEFS join("|", $default_namespace, $subset, $def, "subsetdef")."|\n";
    $subset="";
    $def="";
    @defs=();
}
foreach (@synonym_type_def) {
    @defs = split /"/ ;
    foreach (@defs){
	$subset = stringTrim(@defs[0]);
	$def = stringTrim(@defs[1]);
	$scope = stringTrim(@defs[2]);	
    }
    print SYNTYPEDEFS join("|", $default_namespace, $subset, $def, $scope, "syntypedefs")."|\n";
    $type="";
    $typedef="";
    @typedefs=();
}

print HEADER join("|",$format_version,$data_version,$date,$saved_by,$auto_generated_by,$default_namespace,$remark).$pipe_newline;

