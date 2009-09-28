#!/private/bin/perl 

use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# make sure in the right directory

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/";
chdir "$dir";
print "$dir"."\n" ;

# SubRoutines #

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

#=====================================
# sub initiateVar
# 
# reset the variables to null
#

sub initiateVar  {
    $OttdarTId = "";
    $OttdarGId = "";
    $versionT = "";
    $transType = "";
    $versionG = "";
    $geneType = "";
    $clone = "";
    $transName="";
    $versionG ="";
}

# MAIN # 

use strict;

# declare variables 

my ($OttdarTId,$versionT,$transType,$OttdarGId,$versionG,$geneType,$clone,$transName,$versionG);

my $pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open VEGATS, ">vega_fasta_090403.unl" or die "Cannot open vega_fasta_090113.unl file for write \n";

# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

#$/ = ">" ;

while (<>) {

    next unless /^\>/; 

    my @deflineContent = split /\s/;

    $OttdarTId = @deflineContent[0];
    $OttdarTId =~ s/\>//;
  #  $OttdarTid =~ s/\s//g;
  #  $OttdarTid =~ s/\(/\|/;
  #  $OttdarTid =~ s/\)//;

    $versionT =@deflineContent[4];    

    $transType =@deflineContent[6];

    $OttdarGId =@deflineContent[8];
    
    $geneType =@deflineContent[14];

    $versionG =@deflineContent[12];

#    $geneStatus =@deflineContent[12];
  


    $transName =@deflineContent[2];
    $transName =~ s/\.\d{1}\|/\|/;

    $clone =@deflineContent[18];

    if ($clone =~ m/\S*\,/) {
	my @multiClone = split(',', $clone);
	foreach my $splitClone (@multiClone){
	    
	    $splitClone =~ s/\.\d*//;
	   
	    print VEGATS join ("|",$OttdarTId,$transName,$transType,$versionT,$OttdarGId,$splitClone,$versionG).$pipe_newline;
	}
    }
 
    else {
       $clone =~ s/\.\d*//;

	print VEGATS join ("|",$OttdarTId,$transName,$transType,$versionT,$OttdarGId,$clone,$versionG).$pipe_newline;

    }
#,$OttarGid,$actualGType,$actualGStatus,$actualClone)
# script pulls out coordinates for ottdart.
#.$pipe_newline;

    next;
}
