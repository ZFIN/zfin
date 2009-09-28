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
    $sangerName = "";
    $accNum = "";
    $species="";
    $noDreName = "";
    $seq ="";
    $stemLoop="";
}

# MAIN # 

use strict;

# declare variables 

my ($sangerName,
    $accNum,
    $genus,
    $species,
    $noDreName, $seq, $stemLoop);

#$pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files
system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/*.unl");
system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/mature.*");
system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/hairpin.*");
system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/maturestar.*");

open RNAS, ">matureFa.unl" or die "Cannot open mature.fa file for write \n";
open HRNAS, ">matureHa.unl" or die "Cannot open mature.ha file for write \n";
open MSRNAS, ">msrn.unl" or die "Cannot open mature.ha file for write \n";
# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

#$/ = ">" ;

system("/local/bin/wget -q -q ftp://ftp.sanger.ac.uk/pub/mirbase/sequences/CURRENT/mature.fa.gz -O mature.fa.gz");
system("/local/bin/wget -q -q ftp://ftp.sanger.ac.uk/pub/mirbase/sequences/CURRENT/hairpin.fa.gz -O hairpin.fa.gz");
system("/local/bin/wget -q -q ftp://ftp.sanger.ac.uk/pub/mirbase/sequences/CURRENT/maturestar.fa.gz -O maturestar.fa.gz");


system("/local/bin/gunzip mature.fa.gz");

system("/local/bin/gunzip hairpin.fa.gz");

system("/local/bin/gunzip maturestar.fa.gz");

print "\n hairpin.fa.gz decompressed\n";

open (MRNA, "mature.fa") || die "Cannot open mature.fa : $!\n";
$/ = ">" ;

print RNAS "|";
while (<MRNA>) { 

    s/\n/\|/;

    my @deflineContent = split (/\s/,$_);

    #print RNAS $_;
    
    #print @deflineContent;
    #print @deflineContent[0];
    #print @deflineContent[5];

    $sangerName = @deflineContent[0];
    $accNum = @deflineContent[1];   
    $genus = @deflineContent[2];
    $species = @deflineContent[3];   
    $noDreName = @deflineContent[4];
    $noDreName =~ s/$\s*//;
    $noDreName =~ s/$>//;

    print RNAS join ("|",$sangerName,$accNum,$genus,$species,$noDreName)."|"."\n";
   
}
     
undef $sangerName;
undef $accNum;
undef $genus;
undef $species;
undef $noDreName;

my ($sangerName,
    $accNum,
    $genus,
    $species,
    $noDreName, $seq, $stemLoop);

open (HMRNA, "hairpin.fa") || die "Cannot open hairpin.fa : $!\n";

$/ = ">" ;
while (<HMRNA>) { 

    s/\n//g;

    my @deflineContent = split (/\s/,$_);

    #print RNAS $_;
    
    #print @deflineContent;
    #print @deflineContent[0];
    #print @deflineContent[5];

    $sangerName = @deflineContent[0];
    $accNum = @deflineContent[1];   
    $genus = @deflineContent[2];
    $species = @deflineContent[3];   
    $noDreName = @deflineContent[4];
    $stemLoop = @deflineContent[5];

    print HRNAS join ("|",$sangerName,$accNum,$genus,$species,$noDreName,$stemLoop)."|"."\n";
   
}
undef $sangerName;
undef $accNum;
undef $genus;
undef $species;
undef $noDreName;

my ($sangerName,
    $accNum,
    $genus,
    $species,
    $noDreName, $seq, $stemLoop);


open (MSRNA, "maturestar.fa") || die "Cannot open maturestar.fa : $!\n";
$/ = ">" ;
while (<MSRNA>) { 

    s/\n//g;

    my @deflineContent = split (/\s/,$_);

    #print RNAS $_;
    
    #print @deflineContent;
    #print @deflineContent[0];
    #print @deflineContent[5];

    $sangerName = @deflineContent[0];
    $accNum = @deflineContent[1];   
    $genus = @deflineContent[2];
    $species = @deflineContent[3];   
    $noDreName = @deflineContent[4];
    $noDreName =~ s/\*/\*\|/; 
    $stemLoop = @deflineContent[5];

    print MSRNAS join ("|",$sangerName,$accNum,$genus,$species,$noDreName)."|"."\n";
   
}



#system("/private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/data_transfer/transcript/loadMicroRNA.sql");
