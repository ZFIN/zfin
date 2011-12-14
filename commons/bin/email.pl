#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  Initially, this is only the "beginning of the month,
# tell Dala your vacation and sick leave e-mail

my $zcs = "technical\@zfin.org";
my $zcur = "curators\@zfin.org";
my $zadmin = "knight\@uoneuro.uoregon.edu ";

open MAIL, "|/usr/lib/sendmail -t";

print MAIL "To: dhowe\@zfin.org\n";
print MAIL "Cc: $zcs $zcur $zadmin\n";
print MAIL "Subject: Monthly leave to Eva\n";
print MAIL
    "Please send your vacation and sick leave hour totals for last month\n" .
    "to Eva Quinby <eva\@uoneuro.uoregon.edu>.  You need to\n" .
    "notify her even if you took 0 hours.\n";
close MAIL;
