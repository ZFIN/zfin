#!/private/bin/perl -wT
{
 use CGI;
 use DBI;

 my $Q = new CGI();

 print $Q->header();

  ### the hard coded env paths need a better idea
  $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
  $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
  $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
  ### open a handle on the db
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";

 print "<APPLET archive = \"browser.jar\" codebase = \"/client_apps/Browser\" CODE=\"Browser.class\" NAME =\"Browser\" WIDTH=500 HEIGHT=420>\n" .
	"<param name=format_pre value=\" a.pheno_keywords like '%;\" >\n" .
	"<param name=format_post value=\";%'\" >\n" .
	"<param name=list_label value=\"Keyword Dictionary\">\n" .
	"<param name=selected_label value=\"Selected Keywords\">\n";

 print "<param name = \"data\"   value = \"";
 
 my ($name,$stage,$level,$seq_num);    #temp vars
 my $cur = $dbh->prepare('SELECT name, stage, level, seq_num FROM anatomical_parts ORDER BY seq_num;');
 $cur->execute;
 $cur->bind_col(1, \$name);
 $cur->bind_col(2, \$stage);
 $cur->bind_col(3, \$level);
 $cur->bind_col(4, \$seq_num);
 while ($cur->fetch) {
   print "\n$name|$stage|$level|$seq_num|";
 }
 print "\">\n";
 print "<param name = \"data_delim\"  value = \"|\">\n";
 print "<param name = \"submit\"  value = \"MIval=aa-fishselect.apg\">\n";
 print "<param name = \"querystring\" value = \"" . $Q->url(-relative=>1, -query=>1) . "\">\n";

 if (defined $Q->param('selected_separator')) {
   print "<param name = \"selected_separator\"  value = \"" . $Q->param('selected_separator') . "\">\n";
 }
 my %params = $Q->Vars;
 my $key;
 my $value;

 print "<param name = \"FSList\" value = \"";
 while (($key,$value) = each %params) {
	print "$key|$value|";
 }
 print "\">\n";

# %params.keys();
 my @selected_organs = $Q->param('structure_list');
 print "<param name = \"preselected\" value=\"";
 my $s;
 foreach $s (@selected_organs) {
   print "$s|";
 }
 print "\">\n";
 print "<param name = \"preselected_delim\" value=\"|\">\n";
 print "<param name = \"preselected_format\" value=\"String\">\n";

 print "</APPLET>";

}
