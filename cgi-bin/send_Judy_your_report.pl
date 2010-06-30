#!/private/bin/perl 

use MIME::Lite;

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|CGI_BIN_DIR_NAME|-->";
chdir "$dir";

sub sendLoadReport ($) { # send email on error or completion
    
# . is concantenate
# $_[x] means to take from the array of values passed to the fxn, the 
# number indicated: $_[0] takes the first member.
    
    my $SUBJECT="AutoGen:".$_[0];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[2];
    
    # Create a new multipart message:
    $msg1 = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg1 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
    
}
$filename = "weekly_report_reminder.txt" ;
$emailAddress = "bsprunge\@cs.uoregon.edu,cmpich\@cs.uoregon.edu,judys\@cs.uoregon.edu,kschaper\@cs.uoregon.edu,peirans\@cs.uoregon.edu,pm\@cs.uoregon.edu,tomc\@cs.uoregon.edu,xshao\@cs.uoregon.edu,staylor\@cs.uoregon.edu,ndunn\@uoregon.edu";

$emailHeader = "Weekly status report due";

&sendLoadReport("$emailHeader","$emailAddress","./$filename");

exit;
