#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  This is sent on the 1st of the month to remind us
# to switch.

open MAIL, "|/usr/lib/sendmail -t";

print MAIL "To: staylor\@cs.uoregon.edu\n";
print MAIL "Cc: staylor\@cs.uoregon.edu,bsprunge\@cs.uoregon.edu,kschaper\@cs.uoregon.edu,dhowe\@zfin.org\n";
print MAIL "Subject: Auto: New code checkin person!\n";
print MAIL
    "Its time to switch to the next code-checkin winner: Sierra->Brock->Kevin.\n  Also, we need to change the Fogbugz user to display the next code-checkinner.";

close MAIL;
