#!/private/bin/perl -wT
{
 use CGI;
 use DBI;

 ### open a handle on the db
 my $dbname = "<!--|DB_NAME|-->";
 my $username = "";
 my $password = "";
 ### open a handle on the db
 my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
     or die "\n\nCannot connect to PostgreSQL database: $DBI::errstr\n\n";

 require './mapper_select.pl';
 
 my $Q = new CGI(); 

 print $Q->header();
 print $Q->start_html(-TITLE => "ZFIN Request a Map",-BGCOLOR=>'#FFFFFF');

 print "<script language='JavaScript' src='/javascript/header.js'>
 </script>";

 mapper_select(Q);


 print "<script language='JavaScript' src='/javascript/footer.js'>
 </script>";
 print $Q->end_html();
 $dbh->disconnect;
}
