#!/local/bin/perl 

#
# sp_badgo_rept.pl
#

use MIME::Lite;


#------------------ Send GO Result ----------------
# No parameter
#
sub sendGOResult {

  my $SUBJECT="Auto: Obsolete/secondary GO term in translation files";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";     

  # Create another new multipart message:
  $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg4 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./spkw2go_obsl_secd.txt",
    Filename => "spkw2go_obsl_secd.txt";
  attach $msg4 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./ip2go_obsl_secd.txt",
    Filename => "ip2go_obsl_secd.txt";
  attach $msg4 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./ec2go_obsl_secd.txt",
    Filename => "ec2go_obsl_secd.txt";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);

  close(SENDMAIL);
}


open SPKW, "spkw2go_obsl_secd.unl" or die "Cannot open file spkw2go_obsl_secd.unl";
open SPKW_OUT, ">spkw2go_obsl_secd.txt" or die "Cannot open file spkw2go_obsl_secd.txt";
while (<SPKW>) {
    chomp;  s/\|$//;       #get rid of trailing characters
    s/\|/ .*/g;            #form the regular expression
    my $comd = 'egrep "'.$_.'" spkw2go';    # form the command
    print SPKW_OUT `$comd`;
}
close SPKW;
close SPKW_OUT;

open IP, "ip2go_obsl_secd.unl" or die "Cannot open file ip2go_obsl_secd.unl";
open IP_OUT, ">ip2go_obsl_secd.txt" or die "Cannot open file ip2go_obsl_secd.txt";
while (<IP>) {
    chomp;  s/\|$//;       #get rid of trailing characters
    s/\|/ .*/g;
    my $comd = 'egrep "'.$_.'" interpro2go';    # form the command
    print IP_OUT `$comd`;
}
close IP;
close IP_OUT;

open EC, "ec2go_obsl_secd.unl" or die "Cannot open file ec2go_obsl_secd.unl";
open EC_OUT, ">ec2go_obsl_secd.txt" or die "Cannot open file ec2go_obsl_secd.txt";
while (<EC>) {
    chomp;  s/\|$//;       #get rid of trailing characters
    s/\|/ .*/g;
    my $comd = 'egrep "'.$_.'" ec2go';    # form the command
    print EC_OUT `$comd`;
}
close EC;
close EC_OUT;
       
&sendGOResult();


exit;
