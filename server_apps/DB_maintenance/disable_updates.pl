#!/private/bin/perl -T

use DBI;
use MIME::Lite;

$ENV{PATH} = ""; # for Taint

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

&openSQL();

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 


    open (REVOKE, ">>/tmp/revoke_tables_permission.sql") or die "can not open sql file";
    open (GRANT, ">>/tmp/grant_table_permission.sql") or die "can not open sql file";

$query_revoke = 'select T.tabname from systables T, systabauth A where T.tabid > 99 and T.tabname not like "sys%" and T.tabtype = "T" and A.grantor = "informix" and A.tabid = T.tabid and T.tabtype != "V" and T.tabtype != "Q";';

    my $cur = $dbh->prepare($query_revoke);

    $cur->execute;
    my($tabname);
    $cur->bind_columns(\$tabname);

    $count = 0;
    while ($cur->fetch)
    {
      $tabname = cleanTail($tabname);
      
      $query_revoke_insert = "revoke insert on $tabname from public as informix;";
      $query_revoke_update = "revoke update on $tabname from public as informix;";
      $query_grant_insert = "grant insert on $tabname to public as informix;";
      $query_grant_update = "grant update on $tabname to public as informix;";

    print REVOKE "$query_revoke_insert\n";
    print REVOKE "$query_revoke_update\n";
      
    }

    close(GRANT);
    close(REVOKE);


exit 0;

sub cleanTail () {
  my $var = $_[0];

  while ($var =~ /\000$/) {
    chop ($var);
  }

  return $var;
}

sub openSQL()
  {
    system("/bin/rm -f /tmp/revoke_tables_permission.sql");
    system("touch /tmp/revoke_tables_permission.sql");
    system("/bin/rm -f /tmp/grant_table_permission.sql");
    system("touch /tmp/grant_table_permission.sql");
  }



