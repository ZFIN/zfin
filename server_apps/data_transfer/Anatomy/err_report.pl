
# ----------------- Send Error Report -------------
# Parameter
#   $    Error message 
sub reportError ($) {
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi") || die "Cannot open mailprog!";
    print SENDMAIL "To: <!--|AO_EMAIL_ERR|-->\n"; 
    print SENDMAIL "Subject: AO file generation error\n";

    print SENDMAIL "$_[0]\n"; 
    close(SENDMAIL);
    exit;
}
1
