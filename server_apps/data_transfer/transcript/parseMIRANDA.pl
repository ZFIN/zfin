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

    $dreName = '';
    $ENSDART = '';
    $geneName = '';
}

# MAIN # 

use strict;

# declare variables 
my (
     $dreName, 
     $ENSDART,
     $geneName
);

#$pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

open RNAS, ">v5.txt.danio_rerio.unl" or die "Cannot open v5.txt.danio_rerio file for write \n";
# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

#$/ = ">" ;
#v5.txt.danio_rerio

while (<>) { 

    s/\n/\|/;

    next unless /^Similarity/;                   #skip header and Typedef

    my @deflineContent = split (/\s/,$_);

    #print RNAS $_;
    
    #print @deflineContent;
    #print @deflineContent[0];
    #print @deflineContent[5];

    $dreName = @deflineContent[1];
    $ENSDART = @deflineContent[11];   
    $geneName = @deflineContent[12];

    print RNAS join ("|",$dreName,$ENSDART,$geneName)."\n";
   
}
     
undef $dreName;
undef $ENSDART;
undef $geneName;

system("/private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/loadMIRANDALinks.sql");
