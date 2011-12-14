#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  This is sent on the 20th of the month to remind curators
# to submit monthly project reports to Doug.


my $zcur = "curators\@zfin.org";

open MAIL, "|/usr/lib/sendmail -t";

print MAIL "To: dhowe\@zfin.org\n";
print MAIL "Cc: $zcur\n";
print MAIL "Subject: Monthly project reports to Doug\n";
print MAIL
    "Please send monthly project reports to Doug by the first of the month.\n";
close MAIL;
