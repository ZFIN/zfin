#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  This is sent on the 20th of the month to remind curators 
# to submit monthly project reports to Judy.


my $zcur = "curators\@zfin.org";

open MAIL, "|/usr/lib/sendmail -t";

print MAIL "To: judys\n";
print MAIL "Cc: $zcur\n";
print MAIL "Subject: Monthly project reports to Judy\n";
print MAIL 
    "Please send monthly project reports to Judy by the first of the month.\n";
close MAIL;
