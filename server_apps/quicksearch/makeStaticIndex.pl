#! /private/bin/perl -w 
#------------------------------------------------------------------------
#
# Script that generates a new list of all data pages for the indexer.
# For example, it creates a list URLS of such as:
#   http://quark.zfin.org/action/anatomy/tem-detail/anatomyItem.zdbID=ZDB-ANAT-010921-404
#   http://quark.zfin.org/cgi-bin_quark/webdriver?MIval=aa-markerview.apg&OID=ZDB-BAC-050218-3566
#
# These URLS are then used by the # These URLS are then used by the 
# regenerateindex.pl program to create the QuickSearch indexes.
# 
# Usage:
#
#   makeStaticIndex.pl
#
#   There are no arguments or options.
#
# Author       :  Paea LePendu
# Last Updated :  December 10, 2005

use English;
use DBI;

#------------------------------
# Abort the program with an error message to STDERR
#
# Params 
#  $ $abortStatus 
#  $ $errorText

sub abort($$) {

    my ($line, $abortStatus);
    $abortStatus = $_[0];
    $line = $_[1];
    print (STDERR "Error: $line\n");
    print (STDERR "$PROGRAM_NAME aborted.\n");
    print (STDERR "Exit status: $abortStatus.\n");
    exit ($abortStatus);
}


#------------------------------
# Trim leading and trailing spaces
# using RegEx
sub trim(@) {
	my $str = shift @_;
	for ($str) {           
	  s/^\s+//;
	  s/\s+$//;
	}
	return $str;
}


#------------------------------
# Setting global variables
#

$dbName      = "<!--|DB_NAME|-->";
$dbUsername  = "";
$dbPassword  = "";
$uniqueryDir = "<!--|ROOT_PATH|-->/server_apps/quicksearch";
$filename    = "$uniqueryDir/etc/allAPPPagesList.txt";

# single quotes on purpose (ignores special characters such as '?')
$urlHead     = 'http://<!--|DOMAIN_NAME|-->//cgi-bin/webdriver?MIval=aa-'; 
$urlTail     = '.apg';


#------------------------------
# The following section of code creates a SQL query 
# that gets all app-pages and corresponding
# object id (OID) for each data page in ZFIN.
#
# For example, for the URLs in the above comment, this query gives:
#   anatomy_item  ZDB-ANAT-010921-404
#   markerview    ZDB-BAC-050218-3566
# from which we can construct the URL.
#
# This query was developed with the help of Peiran and Sierra.
#
# Returns:  APP_FILE, OID
my $sql = <<ENDSQL;

-- marker pages, url provided by spl
SELECT "marker" AS app_file, mrkr_zdb_id AS oid, get_mrkr_url(mrkr_zdb_id)
FROM marker

UNION
--feature
SELECT "feature" AS app_file, feature_zdb_id AS oid, "" as url
FROM feature

UNION
-- anatomy_item
SELECT "anatomy_item" AS app_file, term_zdb_id AS oid, "" as url
FROM term where term_ontology = 'zebrafish_anatomy'

UNION

-- companyview
SELECT "companyview" AS app_file, zdb_id AS oid, "" as url
FROM company

UNION

-- crossview
SELECT "crossview" AS app_file, zdb_id AS oid, "" as url
FROM panels

UNION

-- genotypeview
SELECT "genotypeview" AS app_file, geno_zdb_id AS oid, "" as url
FROM genotype

UNION

-- fxfigureview
SELECT "fxfigureview" AS app_file, fig_zdb_id AS oid, "" as url
FROM figure

UNION

-- geneprddescription
-- in this query, we only care about Swiss_Prot external notes
-- the hardcoded FDBCONT identifies such records specifically
SELECT distinct "geneprddescription" AS app_file, dblink_linked_recid, "" as url
FROM db_link
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47'
and exists (
    SELECT 't'
    FROM external_note
    WHERE dblink_zdb_id = extnote_data_zdb_id)

UNION

-- labview
SELECT "labview" AS app_file, zdb_id AS oid, "" as url
FROM lab

UNION

-- markergoview
SELECT distinct "markergoview" AS app_file, mrkrgoev_mrkr_zdb_id, "" as url
FROM marker_go_term_evidence

UNION



-- persview
SELECT "persview" AS app_file, zdb_id AS oid, "" as url
FROM person

UNION

-- sequence
SELECT distinct "sequence" AS app_file, dblink_linked_recid AS oid, "" as url
FROM   db_link, foreign_db_contains, foreign_db_data_type
WHERE  dblink_fdbcont_zdb_id = fdbcont_zdb_id
 AND   fdbdt_pk_id = fdbcont_fdbdt_id
 AND   fdbdt_super_type = "sequence"
-- transcript's have all sequences displayed on their main page
-- and don't use sequence.apg
 AND   dblink_linked_recid[1,11] <> "ZDB-TSCRIPT"
   


UNION

-- xpatexpcdndisplay
SELECT "xpatexpcdndisplay" AS app_file, expcond_zdb_id AS oid, "" as url
FROM experiment_condition

ENDSQL

#------------------------------
# END of SQL query, ENDSQL is necessary to close the print stream.
# Also, the blank line before and after ENDSQL is required syntax.
 
#------------------------------
# Set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

#------------------------------
# MAIN
#
# See top of the file for comments

#------------------------------
# Status
$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Regenerating static index listing.\n");

#------------------------------
# Connect to SQL ZFIN database
$dbh = DBI->connect("DBI:Informix:$dbName", 
			      $dbUsername, 
			      $dbPassword) 
       or die "Cannot connect to Informix database $dbName:$DBI::errstr\n";


#------------------------------
# Open results file for writing
open RESULT, ">$filename" or die "Cannot open $filename for writing results.";

#------------------------------
# Execute the SQL query
my $nRecords = 0;
my $sth = $dbh->prepare($sql) or die "Prepare fails";
$sth->execute();

#------------------------------
# Loop through SQL query, generating results
while (my @row = $sth->fetchrow_array()) {
	$nRecords ++;
	my $app_page = trim($row[0]);
	my $oid = trim($row[1]);
	my $url = trim($row[2]);
	# generate specific URL for data-page corresponding to APP_PAGE, OID pairs
    if ($url eq "") {
	    if ($app_page ne "anatomy_item" ) {
              if ($app_page eq "feature") {
	        $url = "http://<!--|DOMAIN_NAME|-->/action/feature/detail?feature.zdbID=".$oid;
}
else {
    		my $idName = ($app_page eq "xpatexpcdndisplay") ? "&cdp_exp_zdb_id=" : "&OID=";
    		$url = $urlHead . $app_page . $urlTail . $idName . $oid;
}
}
          else { 
	        # hardcode the path for Java page. When Java file path name gets standalized,
            # will update here.
	        $url = "http://<!--|DOMAIN_NAME|-->/action/anatomy/term-detail?anatomyItem.zdbID=".$oid;
	   } 
}
else{
	    #if the url was populated by the query, it won't contain the first bit of the url...
	    $url = "http://<!--|DOMAIN_NAME|-->" . $url; 
	}
	# write URL to file
	print RESULT "$url\n";
}



#The reason we include these in this file is simply so that all the indexed 
#pages are generated from and maintained in this single file.

print RESULT $urlHead . "fxassayabbrev" . $urlTail . "\n";
print RESULT $urlHead . "refcrosslist" . $urlTail . "\n";
print RESULT $urlHead . "wtlist" . $urlTail . "\n";

#------------------------------
# Status
$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Finishing static index.\n");
print ("$dateTime: $nRecords records were processed.\n");

#------------------------------
# Close files
close(RESULT);

#------------------------------
# Call it a day
exit 0
