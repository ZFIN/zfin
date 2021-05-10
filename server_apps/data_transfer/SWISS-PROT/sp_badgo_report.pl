#!/opt/zfin/bin/perl 

#
# sp_badgo_rept.pl
#
# This script take the three unload files which contain obsolete 
# and secondary go term, match each line back to the original entry 
# in the translation table, and rewrite to output files in separate
# categories.
#
# (the three blocks are very silimar and are growing, if they will get even 
# bigger, might better use loop.) 
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

my $print_obst = 0;
my $print_secd = 0;
my $print_ob_se = 0;
open SPKW, "spkw2go_obsl_secd.unl" or die "Cannot open file spkw2go_obsl_secd.unl";
open SPKW_OUT, ">spkw2go_obsl_secd.txt" or die "Cannot open file spkw2go_obsl_secd.txt";
#SP_KW:Chaperone|chaperone activity|0003754|t|f|
while (<SPKW>) {
    my @row = split(/\|/);
    if ($row[3] eq 't' && !$print_obst) {
	print SPKW_OUT "GO term obsolete: \n\n";
	$print_obst = 1;
    }
    if ($row[4] eq 't' && !$print_secd) {
	print SPKW_OUT "\nGO term secondary: \n\n";
	$print_secd = 1;
    }
    if ($row[3] eq 't' && $row[4] eq 't' && !$print_ob_se) {
	print SPKW_OUT "\nGO term obsolete and secondary: \n\n";
	$print_ob_se = 1;
    }
    my $line = join(" .*",$row[0],$row[1],$row[2]);   #form the regular expression
    my $comd = 'egrep "'.$line.'" spkw2go';    # form the command
    print SPKW_OUT `$comd`;
}
close SPKW;
close SPKW_OUT;

my $print_obst = 0;
my $print_secd = 0;
my $print_ob_se = 0;
open IP, "ip2go_obsl_secd.unl" or die "Cannot open file ip2go_obsl_secd.unl";
open IP_OUT, ">ip2go_obsl_secd.txt" or die "Cannot open file ip2go_obsl_secd.txt";
while (<IP>) {
    my @row = split(/\|/);
    if ($row[3] eq 't' && !$print_obst) {
	print IP_OUT "GO term obsolete: \n\n";
	$print_obst = 1;
    }
    if ($row[4] eq 't' && !$print_secd) {
	print IP_OUT "\nGO term secondary: \n\n";
	$print_secd = 1;
    }   
    if ($row[3] eq 't' && $row[4] eq 't' && !$print_ob_se) {
	print IP_OUT "\nGO term obsolete and secondary: \n\n";
	$print_ob_se = 1;
    }
    my $line = join(" .*",$row[0],$row[1],$row[2]);   #form the regular expression
    my $comd = 'egrep "'.$line.'" interpro2go';    # form the command
    print IP_OUT `$comd`;
}
close IP;
close IP_OUT;

my $print_obst = 0;
my $print_secd = 0;
my $print_ob_se = 0;
open EC, "ec2go_obsl_secd.unl" or die "Cannot open file ec2go_obsl_secd.unl";
open EC_OUT, ">ec2go_obsl_secd.txt" or die "Cannot open file ec2go_obsl_secd.txt";
while (<EC>) {
    my @row = split(/\|/);
    if ($row[3] eq 't' && !$print_obst) {
	print EC_OUT "GO term obsolete: \n\n";
	$print_obst = 1;
    }
    if ($row[4] eq 't' && !$print_secd) {
	print EC_OUT "\nGO term secondary: \n\n";
	$print_secd = 1;
    }
    if ($row[3] eq 't' && $row[4] eq 't' && !$print_ob_se) {
	print EC_OUT "\nGO term obsolete and secondary: \n\n";
	$print_ob_se = 1;
    }
    my $line = join(" .*",$row[0],$row[1],$row[2]);   #form the regular expression
    my $comd = 'egrep "'.$line.'" ec2go';    # form the command
    print EC_OUT `$comd`;
}
close EC;
close EC_OUT;
       
&sendGOResult();


exit;
