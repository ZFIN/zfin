#! /private/bin/perl -wT
print "Content-type: text/html\n\n";
#use DBI;
use CGI qw( :cgi );
param("ottdarg") =~ /(OTTDAR[GT][\d]+)/;
my $ottdarg = $1;

my $gene = "";
$ENV{PATH} = ""; # for Taint

#$ENV{INFORMIXDIR} = '<!--|INFORMIX_DIR|-->'; $ENV{INFORMIXSERVER} = '<!--|INFORMIX_SERVER|-->';
#$ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->'; ### open a handle on the db my $dbh =
#DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1}) || die "Failed while
#connecting to <!--|DB_NAME|--> "; my $sth = $dbh->prepare("
#    select mrkr_abbrev
#     from marker,db_link
#     where dblink_acc_num = '$ottdarg'
#     and dblink_linked_recid = mrkr_zdb_id;"
#) or return undef ;
#my $rc  = $sth->execute();
#my @row = $sth->fetchrow_array;
#my $gene = $row[0];
#$dbh->disconnect;

print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>";
print <<_END_
<P>
The Vega record for <b>$gene</b> ($ottdarg) will be available in the Sanger Institute's Vega database shortly. <br>
The annotated transcript(s) are:<P><P><pre>
_END_
;

#system "/private/apps/wublast/xdget -n /research/zusers/tomc/data_transfer/VEGA/vega_trans $ottdarg";
system "/private/apps/wublast/xdget -n /research/zblastdb/db/Current/vega_zfin $ottdarg";
print  "</pre>\n";
print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";

# to make specific... put in postloaddb?
#echo "update foreign_db set fdb_db_query = 'http://beaky.zfin.org/cgi-bin_beaky/pre_vega.cgi?ottdarg=' where fdb_db_name = 'PREVEGA';"| dbaccess tomdb

