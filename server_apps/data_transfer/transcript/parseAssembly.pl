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
    $chromNum = "";
    $Num1 = "";
    $Num2 = "";
    $Num3 = "";
    $cloneName = "";
    $cloneAcc = "";
    $Num4 = "";
    $Num5="";
    $Num6 ="";
}

# MAIN # 

use strict;

# declare variables 

my ($chromNum,$cloneName,$cloneAcc);

my $pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open FILE, ">parsedAssembly.unl" or die "Cannot open parsedAssembly.unl file for write \n";

while (<>) {

    my @deflineContent = split /\s/;

    $chromNum = @deflineContent[0];
    
    $cloneName = @deflineContent[3];
    $cloneAcc = @deflineContent[4];

    $cloneAcc =~ s/\.\d*//;

 
    print FILE join ("|",$chromNum,$cloneName,$cloneAcc).$pipe_newline;

}
