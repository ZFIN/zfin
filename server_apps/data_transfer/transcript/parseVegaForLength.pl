#!/private/bin/perl 

use MIME::Lite;

# set environment variables

#=====================================
# sub initiateVar
# 
# reset the variables to null
#

sub initiateVar  {
    $OttdarTId = "";
    $length="";
}

# MAIN # 

use strict;

# declare variables 

my ($OttdarTId,$length);

my $pipe_newline = "|\n" ;

# initiate variables to null
&initiateVar();

# open output files

open VEGATS, ">vegaWithLengthParsed.txt" or die "Cannot open vegaWithLength.txt file for write \n";

# set the record seperator equal to ">" so that an entire vega record wil be parsed.
# "$/" is the INPUT_RECORD_SEPARATOR and is set equal to " >"

#$/ = ">" ;

print "test1\n";

while (<>) {
    
    next unless /^\>tpe/; 

    my @deflineContent = split /\s/;

    $OttdarTId = @deflineContent[1];
    $OttdarTId =~ s/\>//;
    $length = @deflineContent[12];
    print VEGATS join ("|",$OttdarTId,$length).$pipe_newline;
    next;
}
