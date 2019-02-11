#!/private/bin/perl
# emailOrthologyReports.pl
# 

use MIME::Lite;

# set environment variables

$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

&sendMail("Auto from $dbname: ","<!--|SWISSPROT_EMAIL_REPORT|-->","log/report from the mouse part of orthology scripts","updateMouseOrthologyLog1");
&sendMail("Auto from $dbname: ","<!--|SWISSPROT_EMAIL_ERR|-->","log2 from the mouse part of orthology scripts","updateMouseOrthologyLog2");

&sendMail("Auto from $dbname: ","<!--|SWISSPROT_EMAIL_REPORT|-->","log/report from the human part of orthology scripts","updateHumanOrthologyLog1");
&sendMail("Auto from $dbname: ","<!--|SWISSPROT_EMAIL_ERR|-->","log2 from the human part of orthology scripts","updateHumanOrthologyLog2");

&sendMail("Auto from $dbname: ","<!--|SWISSPROT_EMAIL_REPORT|-->","log/report from the fly part of orthology scripts","updateFlyOrthologyLog1");
&sendMail("Auto from $dbname: ","<!--|SWISSPROT_EMAIL_ERR|-->","log2 from the fly part of orthology scripts","updateFlyOrthologyLog2");


exit;

sub sendMail($) {

    my $SUBJECT=$_[0] .": " .$_[2];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[3]; 
    
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

