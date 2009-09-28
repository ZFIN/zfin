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
    $acc = "";
    $acctype = "";
}

# MAIN # 

use strict;

# declare variables 

my ($OttdarTId,$acc,$acctype);

my $pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open SUPP, ">evidence.unl" or die "Cannot open evidence file for write \n";

# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

#$/ = ">" ;

while (<>) {

    my @deflineContent = split /\s/;

    $OttdarTId = @deflineContent[0];
    $acc = @deflineContent[2];
    $acc =~ s/\.\d{1}//;
    $acctype = @deflineContent[3];
    
    print SUPP join ("|",$OttdarTId,$acc,$acctype).$pipe_newline;

    next;
}
