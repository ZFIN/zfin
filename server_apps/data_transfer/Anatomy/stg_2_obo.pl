#!/private/bin/perl -w
#
# Query STAGE table, generate OBO format output file of stages.
#
# Input: 
#       dbname
#
# Output: STDOUT
#
# Info: source the environment first
#
use strict;
use DBI;
require "err_report.pl";

#======================================================
# main

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
    || &reportError("Failed while connecting to <!--|DB_NAME|--> "); 
   
# ---- print root stage ----
my $stgRootId = "ZFS:0100000"; #this constant is also referred by parseOBO.pl 
print "\n";
print "[Term]\n";
print "id: $stgRootId\n";
print "name: Stages\n";
print "namespace: zebrafish_stages\n";


# ---- query for stage info ----
my $sql = "select stg_zdb_id, stg_obo_id, stg_name
             from stage
         order by stg_obo_id; ";

my $sth = $dbh->prepare($sql) or &reportError("Couldn't prepare the statement:$!\n");
$sth->execute() or &reportError("Couldn't execute the statement:$!\n");

# ---- output each stage as an item, with id, name, parents ----
while (my @data = $sth->fetchrow_array()) {

	my $stgId    = $data[0];
	my $stgOboId = $data[1];
	my $stgName  = $data[2];
	print "\n";
	print "[Term]\n";
	print "id: $stgOboId\n";
	print "name: $stgName\n";
	print "namespace: zebrafish_stages\n";
	print "is_a: $stgRootId ! Stages \n";   
  	print "xref_analog: ZFIN:$stgId\n";
    }

$dbh->disconnect;
exit;
