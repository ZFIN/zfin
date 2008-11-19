#!/private/bin/perl 

#
# sp_badgo_report_ec2gopart.pl
#
# This script take the unload file which contain obsolete 
# and secondary go term, match each line back to the original entry 
# in the translation table, and rewrite to output files in separate
# categories.
#
use MIME::Lite;


#------------------ Send GO Result ----------------
# No parameter
#
sub sendGOResult {

  my $SUBJECT="Auto: Obsolete/secondary GO term in ec2to translation file";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";     

print "\n\nemail to : $MAILTO \n\n";

  # Create another new multipart message:
  $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

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
