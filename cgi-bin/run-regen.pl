#!/private/bin/perl -wT 
{
use CGI  qw / :standard/;
use CGI::Carp 'fatalsToBrowser';
use DBI;
$CGI::POST_MAX=1024;    # max 1K posts
$CGI::DISABLE_UPLOADS = 1;    # no uploads 
my $q = new CGI();
$ENV{"INFORMIXDIR"}      = "<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSQLHOSTS"} = "$ENV{INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"ONCONFIG"}         = "<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSERVER"}   = "<!--|INFORMIX_SERVER|-->";

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|-->  \n $DBI::errstr \n";
my $sth = $dbh->prepare('execute function regen_genomics()'); 
my $rc = $sth->execute();
my @row = $sth->fetchrow;
print $q->header . "\n";   
print $q->start_html(-TITLE => 'Regenerating genomics tables', -bgcolor=> 'white')."\n";
print $q->h1("Regenerating genomics tables");
if( ($rc =~ /0E0/ ) && ($row[0] == 1) ){
	print "<fint color=green>The tables were successfully regenerated.</font>\n";
}
else{
    print "<font color=red>An error occurred regenerating the tables.</font>\n";
}   
print $q->startform(
    -method=> POST,
    -enctype=> "application/x-www-form-urlencoded",
    -name=>   "Return to ZFIN home page",
    -action=> "/<!--|WEBDRIVER_PATH_FROM_ROOT|-->"
); 
print $q->hidden("MIval","aa-ZDB_home.apg");
print $q->end_form(); 
print $q->end_html;
exit;
}
