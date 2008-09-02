#!/private/bin/perl -wT
{
 use CGI;
 use DBI;

 $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
 $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
 $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
 ### open a handle on the db
 my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
 || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";

 require './mapper_select.pl';
 
 my $Q = new CGI(); 

 print $Q->header();
 print $Q->start_html(-TITLE => "ZFIN Request a Map",-BGCOLOR=>'#FFFFFF');

 print "<script language='JavaScript' src='/javascript/header.js'>
 </script>";

 my $cur = $dbh->prepare("select object from webPages where id = 'aa-htmlpageheader.apg';");
 my $tmp_html;
 $cur->execute;
 $cur->bind_col(1, \$tmp_html);
# while($cur->fetch) { print $tmp_html; }

 mapper_select(Q);


 print "<script language='JavaScript' src='/javascript/footer.js'>
 </script>";
 print $Q->end_html();
 $dbh->disconnect;
}
