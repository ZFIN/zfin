#!/private/bin/perl 

use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
$ENV{"INFORMIXSERVER"}="wanda";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";

# make sure in the right directory

$dir = "/research/zcentral/www_homes/swirl/server_apps/data_transfer/transcript/";
chdir "$dir";
print "$dir"."\n" ;

# SubRoutines #

#=====================================
# sub initiateVar
# 
# reset the variables to null
#

sub initiateVar  {  
}

# MAIN # 

use strict;

# declare variables 

my ($lcl,$nuclnum,$tscriptType,$mrelMrkr2,$blastdb,$length,$sequence, $name);

#$pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open StemFASTA, ">zfinStemLoop.fa" or die "Cannot open zfinStemLoop.fa file for write \n";
open (DUMPEDStem, "stemLoopNUCLs.txt") || die "Cannot open stemLoopNUCLs.txt : $!\n";
open matureFASTA, ">zfinMature.fa" or die "Cannot open zfinMature.fa file for write \n";
open (DUMPEDMature, "matureNUCLs.txt") || die "Cannot open stemLoopNUCLs.txt : $!\n";

while (<DUMPEDMature>) { 

    my @deflineContent = split (/\|/,$_);

    $lcl = @deflineContent[0];
    $nuclnum = @deflineContent[1];   
    $mrelMrkr2 = @deflineContent[2];
    $blastdb = @deflineContent[3];
    $tscriptType =@deflineContent[4];
    $name = @deflineContent[5];
    $length = @deflineContent[6];
    $sequence = @deflineContent[7];

    print matureFASTA $lcl."|".$nuclnum."|".$mrelMrkr2." ".$blastdb." ".$tscriptType." ".$name." ".$length."\n ".$sequence."\n"
}
