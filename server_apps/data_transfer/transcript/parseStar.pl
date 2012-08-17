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
# sub initiateVar
# 
# reset the variables to null
#

sub initiateVar  {
  
}

# MAIN # 

use strict;

# declare variables 

my ($acc, $sangerName, $matureName1,$matureName2, $stemloop, $mature1ID, $mature2ID, $mature1seq, $mature2seq);

#$pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open MSRNAS, ">msrnStar.unl" or die "Cannot open mature.ha file for write \n";
# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

open (MRNA, "miRNA.csv") || die "Cannot open miRNA.csv : $!\n";
#$/ = ">" ;


while (<MRNA>) { 

    s/\n/\|/;

    my @deflineContent = split (/\,/,$_);

    $acc = @deflineContent[0];
    $sangerName = @deflineContent[1];   
    $stemloop = @deflineContent[3];
    $mature1ID = @deflineContent[4];
    $matureName1 =@deflineContent[5];
    $matureName2 = @deflineContent[8];
    $mature2ID = @deflineContent[7];
    $mature1seq = @deflineContent[6];
    $mature2seq = @deflineContent[9];

    my $subS = substr($sangerName,0,4);
    print $subS;
    if ($subS eq "dre-"){
    print MSRNAS join ("|",$acc,$sangerName,$matureName1,$matureName2,$stemloop,$mature1ID,$mature2ID,$mature1seq, $mature2seq)."\n";
    }
}
     
undef $acc;
undef $sangerName;
undef $matureName1;
undef $matureName2;
undef $stemloop;
undef $mature1ID;
undef $mature2ID;
undef $mature1seq;
undef $mature2seq;

#system("/private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/loadMicroRNA.sql");
