#!/usr/bin/env bash
#
# Emit a headless (no-CGI) form of cgi-bin/merge_markers.pl to stdout.
#
# This is used only by compare_merge_markers.sh to run the canonical Perl merge in a batch/test
# context. It changes ONLY the input plumbing and connection coordinates -- never the merge logic:
#   - the deploy-time placeholders <!--|DB_NAME|--> / <!--|PGHOST|--> are filled in,
#   - the two CGI->param() reads become $ARGV[0] / $ARGV[1],
#   - `use CGI` / CGI::Carp (absent in the build container) are dropped,
#   - the DB username is set to 'postgres' (trust auth) instead of the empty deploy default,
#   - the DBI handle is opened with RaiseError=1, AutoCommit=0 and an explicit commit before
#     disconnect, so each invocation is atomic. (Production runs the CGI script under autocommit;
#     making the test run atomic matches the Java tool's all-or-nothing transaction, so a pair that
#     errors is a clean no-op on both sides instead of leaving partial state that desyncs a batch.)
#
# Usage: derive_headless_merge_markers.sh <path-to-merge_markers.pl> <db-name> <db-host>
set -euo pipefail

src="$1"
dbname="$2"
dbhost="$3"

DBNAME="$dbname" DBHOST="$dbhost" perl -0777 -pe '
    my $db   = $ENV{DBNAME};
    my $host = $ENV{DBHOST};
    s/^\s*use\s+CGI;\s*$//mg;
    s/^\s*use\s+CGI::Carp[^;]*;\s*$//mg;
    s/my\s+\$data\s*=\s*new\s+CGI\(\);//g;
    s/my\s+\$recordToBeDeleted\s*=\s*\$data->param\("OID"\);/my \$recordToBeDeleted = \$ARGV[0];/;
    s/my\s+\$recordToBeMergedInto\s*=\s*\$data->param\("merge_oid"\);/my \$recordToBeMergedInto = \$ARGV[1];/;
    s/<!--\|DB_NAME\|-->/$db/g;
    s/<!--\|PGHOST\|-->/$host/g;
    s/my\s+\$username\s*=\s*"";/my \$username = "postgres";/;
    s/DBI->connect\s*\(\s*("DBI:Pg:[^"]*")\s*,\s*\$username\s*,\s*\$password\s*\)/DBI->connect($1, \$username, \$password, {RaiseError => 1, AutoCommit => 0})/;
    s/\$dbh->disconnect\(\);/\$dbh->commit;\n\$dbh->disconnect();/;
    s/\$curRegenGenox->execute\([^)]*\);/1; # regen_genox_marker skipped: matches Java --skip-regen (derived denorm recompute; its minute-granular temp table collides in a fast batch)/;
' "$src"
