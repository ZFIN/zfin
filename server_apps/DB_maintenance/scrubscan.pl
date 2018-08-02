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
    "accession_bank:accbk_defline" => 1,
    "blast_hit:bhit_alignment" => 1,
    "blast_hit:bhit_strand" => 1,
    "blast_report:brpt_detail_header" => 1,
    "company:address" => 1,
    "company:bio" => 1,
    "construct_component:cc_component" => 1,
    "curator_session:cs_field_name_value" => 1,
    "data_note:dnote_text" => 1,
    "databasechangelog:comments" => 1,
    "db_link:dblink_info" => 1,
    "external_note:extnote_note" => 1,
    "fest_lib_inst:fli_lib" => 1,
    "figure:fig_caption" => 1,
    "fish:fish_full_name" => 1,            # remove when ZFIN-5905 is resolved
    "fish:fish_name" => 1,                 # remove when ZFIN-5905 is resolved
    "fish:fish_name_order" => 1,           # remove when ZFIN-5905 is resolved
    "fish_components:fc_fish_name" => 1,   # remove when ZFIN-5905 is resolved
    "foreign_db:fdb_db_query" => 1,
    "foreign_db:fdb_url_suffix" => 1,
    "genotype:geno_display_name" => 1,     # remove when ZFIN-5905 is resolved
    "genotype:geno_name_order" => 1,       # remove when ZFIN-5905 is resolved
    "image:img_comments" => 1,
    "lab:address" => 1,
    "lab:bio" => 1,
    "lab_address_update_tracking:laut_new_address" => 1,
    "lab_address_update_tracking:laut_previous_address" => 1,
    "linkage_old:lnkg_comments" => 1,
    "marker:mrkr_abbrev_order" => 1,
    "marker:mrkr_comments" => 1,
    "marker_history_audit:mha_mrkr_abbrev_before" => 1,
    "marker_history_audit:mha_mrkr_name_after" => 1,
    "marker_history_audit:mha_mrkr_name_before" => 1,
    "merge_markers_sql:mms_sql" => 1,
    "panels:mappanel_comments" => 1,
    "person:address" => 1,
    "person:pers_bio" => 1,
    "phenotype_observation_generated:psg_e1a_name" => 1,
    "phenotype_observation_generated:psg_e2a_name" => 1,
    "phenotype_observation_generated:psg_short_name" => 1,
    "phenotype_observation_generated_bkup:psg_e1a_name" => 1,
    "phenotype_observation_generated_bkup:psg_e2a_name" => 1,
    "phenotype_observation_generated_bkup:psg_short_name" => 1,
    "phenotype_observation_generated_temp:psg_e1a_name" => 1,
    "phenotype_observation_generated_temp:psg_e2a_name" => 1,
    "phenotype_observation_generated_temp:psg_short_name" => 1,
    "probe_lib:pl_description" => 1,
    "probe_lib:pl_develop_stage" => 1,
    "probe_lib:pl_lib_name" => 1,
    "probe_library:probelib_name" => 1,
    "probe_library:probelib_restriction_sites" => 1,
    "pub_correspondence_received_email:pubcre_subject" => 1,
    "pub_correspondence_received_email:pubcre_text" => 1,
    "pub_correspondence_sent_email:pubcse_text" => 1,
    "pub_correspondence_subject:pcs_subject_text" => 1,
    "pub_correspondence_subject:pcs_subject_type" => 1,
    "publication:authors" => 1,
    "publication:pub_errata_and_notes" => 1,
    "publication_file:pf_file_name" => 1,
    "publication_file:pf_original_file_name" => 1,
    "publication_note:pnote_text" => 1,
    "sequence_feature_chromosome_location_bkup:sfcl_location_source" => 1,
    "sequence_feature_chromosome_location_bkup:sfcl_location_subsource" => 1,
    "sequence_feature_chromosome_location_generated:sfclg_location_source" => 1,
    "sequence_feature_chromosome_location_generated:sfclg_location_subsource" => 1,
    "sequence_feature_chromosome_location_generated_bkup:sfclg_location_source" => 1,
    "sequence_feature_chromosome_location_generated_bkup:sfclg_location_subsource" => 1,
    "sequence_feature_chromosome_location_generated_temp:sfclg_location_source" => 1,
    "sequence_feature_chromosome_location_generated_temp:sfclg_location_subsource" => 1,
    "sequence_feature_chromosome_location_temp:sfcl_location_source" => 1,
    "sequence_feature_chromosome_location_temp:sfcl_location_subsource" => 1,
    "term:term_comment" => 1,
    "term:term_definition" => 1,
    "term:term_name" => 1,
    "updates:comments" => 1,
    "updates:new_value" => 1,
    "updates:old_value" => 1,
    "zdb_object_type:zobjtype_name" => 1, # make a case for this?
);


# get the names of all the tables and text/character columns in the database.
my $sql = "SELECT tab.table_name, col.column_name, col.data_type
           FROM information_schema.tables tab
           INNER JOIN information_schema.columns col ON tab.table_name = col.table_name
           WHERE tab.table_schema = 'public'
           AND col.table_schema = 'public'
           AND tab.table_type = 'BASE TABLE'
           AND col.data_type IN ('character varying', 'character', 'text')
           AND tab.table_name NOT LIKE 'tmp_%'
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