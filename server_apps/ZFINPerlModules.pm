#!/usr/bin/perl
package ZFINPerlModules;

use strict;
use MIME::Lite;

sub sendMailWithAttachedReport {
    my $MAILTO = $_[1];
    my $SUBJECT = $_[2];
    my $TXTFILE = $_[3]; 
    
    print "\n\nMAILTO ::: $MAILTO  \n";
    print "\n\nSUBJECT ::: $SUBJECT  \n";
    print "\n\nTXTFILE ::: $TXTFILE  \n\n\n";

    # Create a new multipart message:
    my $msg = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg->print(\*SENDMAIL);
    close (SENDMAIL);

}

1;
