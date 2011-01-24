#!/private/bin/perl -T

use DBI;
use MIME::Lite;

$ENV{PATH} = ""; # for Taint

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/DB_maintenance/revoke_tables_permissions.sql");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/DB_maintenance/grant_tables_permissions.sql"); 
openSQL();

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 


    open (REVOKE, ">>revoke_tables_permission.sql") or die "can not open sql file";
    open (GRANT, ">>grant_table_permission.sql") or die "can not open sql file";

    $query_select = 'select tabname from systables where tabid > 99 and tabname not like "sys%" and tabtype = "T";';

    my $cur = $dbh->prepare($query_select);

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
    print GRANT "$query_grant_insert\n";
    print GRANT "$query_grant_update\n";
      
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
    system("/bin/rm -f revoke_table_permission.sql");
    system("touch revoke_table_permission.sql");
    system("/bin/rm -f grant_table_permission.sql");
    system("touch grant_table_permission.sql");
  }



