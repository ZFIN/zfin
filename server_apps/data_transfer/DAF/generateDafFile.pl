#!/private/bin/perl -w

use strict;
use DBI;

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my $mailprog = '/usr/lib/sendmail -t -oi -oem';

my $output = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/daf.other";

$versionNumber += 0.1;

open (UNL, ">disease_association.ZFIN.txt") or die "Cannot open disease_association.ZFIN.txt";

print UNL "!daf_version: 0.1\n";
print UNL "!Date: ".`/bin/date +%Y/%m/%d`;
print UNL "!Project_name: ZFIN (zfin.org) \n";
print UNL "!URL: http://zfin.org\n";
print UNL "!Contact Email: zfinadmn@zfin.org\n";





open (OUT,">$output") or &emailError ("Can not open $output to write");

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', '', 
                       {AutoCommit => 1,RaiseError => 1}
                      )
    || &emailError("Failed while connecting to <!--|DB_NAME|--> "); 

my $sql = "select ;";

my $sth = $dbh->prepare($sql);
$sth->execute;
$sth->bind_columns(\$, \$, \$, \$);

while ($sth->fetch) {
