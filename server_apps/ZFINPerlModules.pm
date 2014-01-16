#!/usr/bin/perl
package ZFINPerlModules;

use strict;
use MIME::Lite;
use DBI;

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
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
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

sub stringStartsWithLetter() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[a-zA-Z]/) {
      return 1;
  } else {
      return 0;
  }
}

1;
