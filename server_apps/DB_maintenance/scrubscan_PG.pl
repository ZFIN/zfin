#!/private/bin/perl -w
#------------------------------------------------------------------------
#
# Script to scan all occurrences of char and varchar columns in all tables 
# for conditions that we don't want to happen.  These conditions are those
# that can be fixed by the scrub_char() function.  Any columns reported by
# this script should be examined and either:
#  1. Added to this script's avoid list, in which case the script will no
#     longer check or report that column.
#  2. Identify how the bad data got in.
#     A. If it got in through a script then modify the script to prevent it
#        from happening again, and fix the data.
#     B. If it got in through the user interface then add update and insert
#        triggers on the column to always call scrub_char().
#
# Usage:
#   scrubscan.pl
#
# There are no arguments.
#

#------------------------------------------------------------------------
# Main.
#

use English;			# enable longer special variable names

use lib "$ENV{'ROOT_PATH'}/server_apps/";
use ZFINPerlModules;

#
# Define GLOBALS
#

# ain't got none, everything in main. (put another way - they are all globals!)

my $dbname = $ENV{'DBNAME'};
my $username = "";
my $password = "";

### open a handle on the db
my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password) or die "Cannot connect to database: $DBI::errstr\n";


# Define tests:
# 3 items: Test Name:Apply to Char columns?:Condition
my @tests = 
  ("LeadSpace:Y:column like ' %' and column <> ' '",
   "DoubleSpace:Y:column like '_%  %_' and column <> ' '",
   "AllSpace:Y:column = ' ' and octet_length(column) > 0",
   "EmptyStrng:N:octet_length(column) = 0",
   # "Newlines:Y:column like \"%",
   # %\"", 
   # "Tabs:Y:column like \"%	%\"",
   "TrailSpace:N:length(column) <> octet_length(column) and column <> ' '",
   "ScrubChar:Y:column <> scrub_char(column)");


# Define columns to avoid:
# 2 items: table name:column name
my %avoids = (
    "acc_ref_gid_eid:ref" => "avoid",	# causes syntax errors
    "db_link:dblink_info" => "avoid",   # not displayed anywhere
    "defline:dfln_abbrev" => "avoid",   # not really our data
    "defline:dfln_name"   => "avoid",     # not really our data
    "defline:dfln_defline" => "avoid",  # not really our data
    "fest_lib_inst:fli_lib" => "avoid",	# Tom C says leave it be.
    "probe_lib:pl_lib_name" => "avoid",	# Tom C says leave it be.
    "probe_lib:pl_develop_stage" => "avoid",	# Tom C says leave it be.
    "probe_library:probelib_name" => "avoid",	# Tom C says leave it be.
    "probe_library:probelib_restriction_sites" => "avoid", # Tom C says leave it be.
    "xpat_anatomy_capture:xac_anatitem_name" => "avoid" # bad user input 
);


# get the names of all the tables and text/character columns in the database.
my $sql = "SELECT tab.table_name, col.column_name, col.data_type
           FROM information_schema.tables tab
           INNER JOIN information_schema.columns col ON tab.table_name = col.table_name
           WHERE tab.table_schema = 'public'
           AND col.table_schema = 'public'
           AND tab.table_type = 'BASE TABLE'
           AND col.data_type IN ('character varying', 'character', 'text')
           ORDER BY tab.table_name;";

my $cur = $dbh->prepare($sql);
$cur->execute();

my $tableName;
my $columnName;
my $columnType;

$cur->bind_columns(\$tableName, \$columnName, \$columnType);

my $firstLineOut = 1;
my $totalRows = 0;
while ($cur->fetch()) {
    if ($avoids{"$tableName:$columnName"}) {
        next;
    }
    foreach $test (@tests) {
        ($testName, $applyToChar, $condition) = split(/:/,$test);
        if ($applyToChar eq "N" && $columnType eq "character") {
            next;
        }

        $condition =~ s/column/$columnName/g;
        $sql = "SELECT COUNT($columnName) FROM $tableName WHERE $condition";
        my ($nRowsFound) = $dbh->selectrow_array($sql);
        $totalRows += $nRowsFound;

        if ($nRowsFound) {
            # write tablename and rest of results to output.
            if ($firstLineOut) {
                $firstLineOut = 0;
                print("\n############################################################################\n");
                print("########################### SCRUBSCAN ######################################\n\n");
                print("                                                                      # Rows\n");
                print("Table Name                  Column Name                    Test       Failed\n");
                print("--------------------------- ------------------------------ ---------- ------\n");
            }
            printf("%27s %-30s %-10s %6d\n", $tableName, $columnName, $testName, $nRowsFound);
        }
    }
}

exit $totalRows;