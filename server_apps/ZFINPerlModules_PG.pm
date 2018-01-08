#!/usr/bin/perl
package ZFINPerlModules;

use strict;
use MIME::Lite;
use DBI;

my %monthDisplays = (
    "Jan" => "01",
    "Feb" => "02",
    "Mar" => "03",
    "Apr" => "04",
    "May" => "05",
    "Jun" => "06",
    "Jul" => "07",
    "Aug" => "08",
    "Sep" => "09",
    "Oct" => "10",
    "Nov" => "11",
    "Dec" => "12"
);


sub doSystemCommand {                  

  my $systemCommand = $_[1];               

  print "Executing [$systemCommand] \n";

  my $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) {
    exit -1;
  }
} 


sub sendMailWithAttachedReport {
    my $MAILTO = $_[1];
    my $SUBJECT = $_[2];
    my $TXTFILE = $_[3]; 
    
    print "\n\nMAILTO ::: $MAILTO  \n";
    print "\n\nSUBJECT ::: $SUBJECT  \n";
    print "\n\nTXTFILE ::: $TXTFILE  \n\n\n";

    # Create a new multipart message:
    my $msg = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg->print(\*SENDMAIL);
    close (SENDMAIL);

}

sub countData() {

  my $ctsql = $_[1];
  my $nRecords = 0;

  ### open a handle on the db
    my $dbh = DBI->connect('DBI:Pg:dbname=<!--|DB_NAME|-->;host=localhost',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to <!--|DB_NAME|-->");


  my $sth = $dbh->prepare($ctsql) or die "Prepare fails";
  
  $sth -> execute() or die "Could not execute $ctsql";
  
  while (my @row = $sth ->fetchrow_array()) {
    $nRecords++;
  }  

  $dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";

  return ($nRecords);
}

sub stringStartsWithNumber() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[0-9]/) {
      return 1;
  } else {
      return 0;
  }
}

sub stringStartsWithLetter() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[a-zA-Z]/) {
      return 1;
  } else {
      return 0;
  }
}

sub stringStartsWithLetterOrNumber() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[a-zA-Z0-9]/) {
      return 1;
  } else {
      return 0;
  }
}


sub getYear() {
  my $dateString = $_[1];
  my $year = substr $dateString, 0, 4;
  return $year;
}

sub getMonth() {
  my $dateString = $_[1];
  my $month = substr $dateString, 4, 2;
  return $month;
}

sub getDay() {
  my $dateString = $_[1];
  my $day = substr $dateString, 6;
  return $day;
}

sub month3LettersToNumber() {
  my $month3Letters = $_[1];
  if (exists($monthDisplays{$month3Letters})) {
      return $monthDisplays{$month3Letters};
  } else {
      return "-1";
  }
}


1;
