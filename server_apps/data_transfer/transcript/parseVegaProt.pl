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
    $OttdarPId = "";
    $OttdarGId = "";
    $OttdarTId = "";
}

# MAIN # 

use strict;

# declare variables 

my ($OttdarPId,$OttdarGId,$OttdarTId);

my $pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open PROTS, ">ottdarp.unl" or die "Cannot open ottdarp.unl file for write \n";

# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

#$/ = ">" ;

while (<>) {

    next unless /^\>/; 

    my @deflineContent = split /\s/;

    $OttdarPId = @deflineContent[0];
    $OttdarPId =~ s/\>//;

    $OttdarGId =@deflineContent[3];
    $OttdarGId =~ s/Gene://;
    
    $OttdarTId =@deflineContent[4];
    $OttdarTId =~ s/Transcript://;

    print PROTS join ("|",$OttdarTId,$OttdarGId,$OttdarPId).$pipe_newline;

    next;
}
