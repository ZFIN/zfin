#!/private/bin/perl -wT
# file runSPL.cgi
# author Tom Conlin, Jan. 20 2001
# expects input a param named "stored_procdure" containing 
# the name stored procedure to be run. 
# currently the SPL is not passed any arguments  
# the SPL is expected to return the integer 1 on succuss 
# and the integer 0 for failure (a historical artifact)
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
my $stored_procedure = $q->param('stored_procedure');
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|-->  \n $DBI::errstr \n";
my $sth = $dbh->prepare("execute function $stored_procedure()");
my $rc = $sth->execute();
my @row = $sth->fetchrow;

print $q->header . "\n";
print $q->start_html(-TITLE => "Running $stored_procedure", -bgcolor=> 'white')."\n";
print $q->h1("Running $stored_procedure");
if( ($rc =~ /0E0/ ) && ($row[0] == 1) ){
    print "<p><font color=green>$stored_procedure successfully run.</font><p>\n";
}
else{
    print "<p><font color=red>An error occurred running $stored_procedure.</font><p>\n";
}

print 
$q->start_form(-method=>'POST',-action=>'/<!--|WEBDRIVER_PATH_FROM_ROOT|-->'). 
$q->hidden('MIval','aa-ZDB_home.apg').
$q->submit('Return to ZFIN home page'). 
$q->end_form;

print $q->end_html;
exit;
}
