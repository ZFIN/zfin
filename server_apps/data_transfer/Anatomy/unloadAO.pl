#!/private/bin/perl -w
#
# This script writes obo file header, calls
# anatitem_2_obo.pl and stg_2_obo.pl to generate
# obo format ZFIN anatomy file.
#
# Input:
#   share flag :: optional. If share is specified, the file output goes
#                 goes to OBO site, otherwise, to curators. This gets
#                 passed into anatitem_2_obo.pl to decide if 
#                 anatitem_description will be outputed to obo file as Comment.
#                
#
# Output:
#   OBO format AO file sent by email
# 

use strict;
use MIME::Lite
require "err_report.pl";

#------------------ Send OBO File----------------
# No parameter
#
sub sendResult {

    my $input_shareFlag = $_[0];
    my $file_domain = $input_shareFlag ? "for public" : "for ZFIN";

    my $SUBJECT="Auto: OBO file ".$file_domain;
    my $MAILTO="<!--|AO_EMAIL_CURATOR|-->";   
    my $ATTFILE ="./zfin.obo";
    
    # Create a new multipart message:
    my $msg = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';
    
    attach $msg 
	Type     => 'application/octet-stream',
	Encoding => 'base64',
	Path     => "./$ATTFILE",
	Filename => "$ATTFILE";
    
    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg->print(\*SENDMAIL);
    
    close(SENDMAIL);
}


#--------------- Main --------------------------------
#
my $shareFlag = $ARGV[0] ? $ARGV[0] : "";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Anatomy/";

#remove old files
system("/bin/rm -f *.obo");

open (OUT, ">zfin.obo") or &reportError("Cannot open file to write.");

#----  print header ------
my ($sec, $min, $hour, $mday, $month,$year) = localtime(time);
$year += 1900;
$month += 1;

my $typedef = <<END;

[Typedef]
id: part_of
name: part of

[Typedef]
id: develops_from
name: develops from

[Typedef]
id: start
name: start stage

[Typedef]
id: end
name: end stage

END

print OUT "format-version: 1.0\n";
print OUT "date: $mday:$month:$year $hour:$min\n";
print OUT "saved-by: ZFIN\n";
print OUT "default-namespace: zebrafish_anatomical_ontology\n";
print OUT "$typedef";

#-- invoke scripts to generate anatomy items and stage items

print OUT `./anatitem_2_obo.pl $shareFlag`;

print OUT `./stg_2_obo.pl`;

close OUT;

&sendResult($shareFlag);

exit;
