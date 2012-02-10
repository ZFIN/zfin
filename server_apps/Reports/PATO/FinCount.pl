#! /private/bin/perl -w
#
# FinCount.pl
#
# This script is called from server_apps/Reports/Count.pl. 
# Count how many of the new phenotypes are added monthly with fin phenotypes, including substructures
# See FB case 7788 and 8007 for more information.
#
use strict;
use MIME::Lite;
use DBI;

#------------------ Send Result ----------------
#
#
sub sendResult ($$$){

  my $SUBJECT=$_[0];
  my $MAILTO=$_[1];
  my $TXTFILE=$_[2];

  # Create a new multipart message:
  my $msg = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg
   Type     => 'text/plain',
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg->print(\*SENDMAIL);

  close(SENDMAIL);
}

#------------------ Main -------------------------
# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";


chdir "<!--|ROOT_PATH|-->/server_apps/Reports/PATO";

print "\nStart counting Fin phenotypes\n\n";

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";
    
my $cur = $dbh->prepare('select TODAY from organism where organism_common_name = ?;');
$cur->execute("Zebrafish");
my $today;
$cur->bind_columns(\$today);                       
while ($cur->fetch()) {
}
$cur->finish(); 

print "\n  today is $today\n\n" ;

$dbh->disconnect();  

my @todayFields = split(/\//, $today);
my $month = $todayFields[0];
my $date = $todayFields[1];
my $year = $todayFields[2];

my $preMonth = "";

if ($month eq "01") {
    $preMonth = "12";      
} elsif ($month eq "02") {
    $preMonth = "01"; 
} elsif ($month eq "03") {
    $preMonth = "02";
} elsif ($month eq "04") {
    $preMonth = "03";
} elsif ($month eq "05") {
    $preMonth = "04"; 
} elsif ($month eq "06") {
    $preMonth = "05";
} elsif ($month eq "07") {
    $preMonth = "06"; 
} elsif ($month eq "08") {
    $preMonth = "07";
} elsif ($month eq "09") {
    $preMonth = "08";
} elsif ($month eq "10") {
    $preMonth = "09"; 
} elsif ($month eq "11") {
    $preMonth = "10";
} elsif ($month eq "12") {
    $preMonth = "11"; 
} else {
    print "\n wrong month:  $month\n";   
    exit;
}

my $prevYear;
if ($month eq "01") {
    $year = int($year);
    $prevYear = $year - 1;
} else {
    $prevYear = $year;
}

#my $todayStr = "2012-02-01 00:00:00";

my $todayStr = $year."-".$month."-".$date." "."00:00:00";
my $oneMonthAgoStr = $prevYear."-".$preMonth."-".$date." "."00:00:00";

print "\n  todayStr is $todayStr\n" ;
print "\n  oneMonthAgoStr is $oneMonthAgoStr\n\n" ;

system("rm -f finPhenoCount.sql");
open SQLFILE, ">finPhenoCount.sql" || die ("finPhenoCount.sql !");

print SQLFILE "\nbegin work;\n\n";
print SQLFILE "create temp table  tmp_contains \n";
print SQLFILE "(  \n";
print SQLFILE "   id serial8 \n";
print SQLFILE ") with no log; \n\n" ;

print SQLFILE "insert into tmp_contains \n";
print SQLFILE "select phenos_pk_id  \n" ; 
print SQLFILE "from phenotype_experiment, publication, phenotype_statement, figure, term, all_term_contains \n";
print SQLFILE "where fig_source_zdb_id = zdb_id \n" ;
print SQLFILE "and fig_zdb_id = phenox_fig_zdb_id \n" ;
print SQLFILE "and phenox_pk_id = phenos_phenox_pk_id \n" ;
print SQLFILE "and phenos_entity_1_superterm_zdb_id = alltermcon_contained_zdb_id \n" ;
print SQLFILE "and term_zdb_id = alltermcon_container_zdb_id \n" ;
print SQLFILE "and term_name = \"fin\" \n" ;
print SQLFILE "and jtype != \"Curation\" \n" ;
print SQLFILE "and jtype != \"Unpublished\" \n" ;
print SQLFILE "and phenos_created_date between \"$oneMonthAgoStr\" and \"$todayStr\"; \n\n" ;

print SQLFILE "insert into tmp_contains \n";
print SQLFILE "select phenos_pk_id  \n" ; 
print SQLFILE "from phenotype_experiment, publication, phenotype_statement, figure, term, all_term_contains \n";
print SQLFILE "where fig_source_zdb_id = zdb_id \n" ;
print SQLFILE "and fig_zdb_id = phenox_fig_zdb_id \n" ;
print SQLFILE "and phenox_pk_id = phenos_phenox_pk_id \n" ;
print SQLFILE "and phenos_entity_2_superterm_zdb_id = alltermcon_contained_zdb_id \n" ;
print SQLFILE "and term_zdb_id = alltermcon_container_zdb_id \n" ;
print SQLFILE "and term_name = \"fin\" \n" ;
print SQLFILE "and jtype != \"Curation\" \n" ;
print SQLFILE "and jtype != \"Unpublished\" \n" ;
print SQLFILE "and phenos_created_date between \"$oneMonthAgoStr\" and \"$todayStr\"; \n\n" ;

print SQLFILE "insert into tmp_contains \n";
print SQLFILE "select phenos_pk_id  \n" ; 
print SQLFILE "from phenotype_experiment, publication, phenotype_statement, figure, term, all_term_contains \n";
print SQLFILE "where fig_source_zdb_id = zdb_id \n" ;
print SQLFILE "and fig_zdb_id = phenox_fig_zdb_id \n" ;
print SQLFILE "and phenox_pk_id = phenos_phenox_pk_id \n" ;
print SQLFILE "and phenos_entity_1_subterm_zdb_id = alltermcon_contained_zdb_id \n" ;
print SQLFILE "and term_zdb_id = alltermcon_container_zdb_id \n" ;
print SQLFILE "and term_name = \"fin\" \n" ;
print SQLFILE "and jtype != \"Curation\" \n" ;
print SQLFILE "and jtype != \"Unpublished\" \n" ;
print SQLFILE "and phenos_created_date between \"$oneMonthAgoStr\" and \"$todayStr\"; \n\n" ;

print SQLFILE "insert into tmp_contains \n";
print SQLFILE "select phenos_pk_id  \n" ; 
print SQLFILE "from phenotype_experiment, publication, phenotype_statement, figure, term, all_term_contains \n";
print SQLFILE "where fig_source_zdb_id = zdb_id \n" ;
print SQLFILE "and fig_zdb_id = phenox_fig_zdb_id \n" ;
print SQLFILE "and phenox_pk_id = phenos_phenox_pk_id \n" ;
print SQLFILE "and phenos_entity_2_subterm_zdb_id = alltermcon_contained_zdb_id \n" ;
print SQLFILE "and term_zdb_id = alltermcon_container_zdb_id \n" ;
print SQLFILE "and term_name = \"fin\" \n" ;
print SQLFILE "and jtype != \"Curation\" \n" ;
print SQLFILE "and jtype != \"Unpublished\" \n" ;
print SQLFILE "and phenos_created_date between \"$oneMonthAgoStr\" and \"$todayStr\"; \n\n\n" ;

print SQLFILE "! echo \"Number of phenotype data with fin (including substructures) added between $oneMonthAgoStr and $todayStr: \" ;\n\n";

print SQLFILE "\nselect count (distinct id) from tmp_contains;\n\n\n";

print SQLFILE "rollback work;\n\n";

close(SQLFILE);

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> finPhenoCount.sql > FinPhenotypeStatistics 2> err");

&sendResult("Monthly Fin Phenotype statistics", "<!--|COUNT_PATO_OUT|-->","./FinPhenotypeStatistics");
&sendResult("Monthly Phenotype statistics Err", "xshao\@cs.uoregon.edu", "./err");

exit;
