#! /opt/zfin/bin/perl -w 

use Getopt::Long qw(:config bundling);
use DBI;

use MIME::Lite;

$ENV{"INFORMIXDIR"}      = "<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}   = "<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}         = "onconfig";
$ENV{"INFORMIXSQLHOSTS"} = "<!--|INFORMIXSQLHOSTS|-->";


my $dbname = $ENV{"DBNAME"};

my $dbh = DBI->connect("DBI:Informix:$dbname",
		       '', 
		       '', 
		       {AutoCommit => 1,RaiseError => 1}
    )
    || emailError("Failed while connecting to $dbname "); 


my $sql_command = "select tabname, colname, collength from syscolumns, systables where systables.tabid = syscolumns.tabid and coltype in (13,43) and systables.tabid > 110 order by systables.tabid asc;";

my $cur = $dbh->prepare($sql_command);
$cur->execute();

$logFile = "/tmp/tablesWithVarchars.sql";
system("/bin/rm -f /tmp/tablesWithVarchars.sql");

open(LOG, ">$logFile") or abort("Cannot open log file $logFile.\n");

while (my $results = $cur->fetchrow_hashref) {
    print (LOG "select count($results->{colname}) as counter,\"$results->{colname}\" as column,\"$results->{tabname}\" as tabname from $results->{tabname} where $results->{colname} is not null and $results->{colname} != \"\" and octet_length($results->{colname}) = $results->{collength};"."\n");

}
close(LOG);


open(TABLES, "</tmp/tablesWithVarchars.sql")  or abort("Cannot open table file /tmp/tablesWithVarchars.sql\n");
open(REPORT, ">/tmp/tablesNeedColumnLengthAdjusted.txt");

while (<TABLES>) {
    $sql_command = $_;
    $cur = $dbh->prepare($sql_command);
    $cur->execute();
    my $tables = $cur->fetchrow_hashref;
    my $counter = $tables->{counter};
    if ($counter ne 0 ) {
	print (REPORT "table:   $tables->{tabname}"."\n") ;
        print (REPORT "column:  $tables->{column}"."\n");
        print (REPORT "numRows: $tables->{counter}"."\n");
	print (REPORT "\n");
    }

}



close(TABLES);
close(REPORT);

&sendMail("Auto from $dbname: checkVarcharOctetLength.pl : ","<!--|VALIDATION_EMAIL_DBA|-->","tables needing column length adjustment","/tmp/tablesNeedColumnLengthAdjusted.txt");

$dbh->disconnect;

sub sendMail($) {

    my $SUBJECT=$_[0] .": " .$_[2];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[3]; 
    
    # Create a new multipart message:
    $msg1 = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg1 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
    
}


    exit 0;
