#!/private/bin/perl

# PubMed records are UTF-8 encoded. Informix is currently (as of August 2014)
# using ISO-8859-1. This means that PubMed text should not go directly into the
# database without re-encoding the text. The fetchPubsFromPubMed.pl script did
# not initially do that. That script has been updated, but some incorrectly
# encoded text did get into the database. This script attempt to detect
# publications with incorrectly encoded character and fix them. It outputs two
# files:
#
#    * fixCharsUpdated.txt   a list of ZDB-IDs for publications which had at
#                            at least one field updated
#    * fixCharsErrors.txt    a list of ZDB-IDs for publications which contained
#                            an unresolvable character and were not updated.
#                            These publications need to be fixed manually.

use charnames ':full';
use DBI;
use Encode;

open(ERRS, ">fixCharsErrors.txt");
open(PUBS, ">fixCharsUpdated.txt");

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

# regex to detect multi-byte sequences that look like they were supposed to be a
# single UTF8-encoded character.
$questionable = '[\xC0-\xDF][\x80-\xBF]|' .
                '[\xE0-\xEF][\x80-\xBF]{2}|' .
                '[\xF0-\xF7][\x80-\xBF]{3}|' .
                '[\xF8-\xFB][\x80-\xBF]{4}|' .
                '[\xFC-\xFD][\x80-\xBF]{5}';

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password)
  || die("Failed while connecting to <!--|DB_NAME|--> ");

%pub = (
  zdb_id => undef,
  authors => undef,
  title => undef,
  keywords => undef,
  pub_abstract => undef,
  pub_errata_and_notes => undef,
  pubmed_authors => undef,
  pub_mini_ref => undef,
  pub_authors_lower => undef
);

$fetch_pub_sql = <<'END_SQL';
SELECT zdb_id,
       authors,
       title,
       keywords,
       pub_abstract,
       pub_errata_and_notes,
       pubmed_authors,
       pub_mini_ref,
       pub_authors_lower
FROM   publication;
END_SQL

$update_pub_sql = <<'END_SQL';
UPDATE publication
   SET authors = ?,
       title = ?,
       keywords = ?,
       pub_abstract = ?,
       pub_errata_and_notes = ?,
       pubmed_authors = ?,
       pub_mini_ref = ?,
       pub_authors_lower = ?
 WHERE zdb_id = ?;
END_SQL

$add_update_sql = <<'END_SQL';
INSERT INTO updates (rec_id, field_name, new_value, old_value, comments, when)
VALUES (?, ?, ?, ?, ?, current);
END_SQL

$all_pubs = $dbh->prepare($fetch_pub_sql);
$all_pubs->bind_columns(\$pub{zdb_id},
                        \$pub{authors},
                        \$pub{title},
                        \$pub{keywords},
                        \$pub{pub_abstract},
                        \$pub{pub_errata_and_notes},
                        \$pub{pubmed_authors},
                        \$pub{pub_mini_ref},
                        \$pub{pub_authors_lower});
$update_pub = $dbh->prepare($update_pub_sql);
$add_update = $dbh->prepare($add_update_sql);

$all_pubs->execute();
while ($all_pubs->fetchrow_arrayref) {
  eval {
    $needs_update = 0;
    foreach $field (keys %pub) {
      $old_value = $pub{$field};
      if ($pub{$field} =~ s/($questionable)/do_replacement($1)/ge) {
        $needs_update = 1;
        $add_update->execute($pub{zdb_id}, $field, $pub{$field}, $old_value, "fix character encoding problem");
      }
    }
    if ($needs_update) {
      $update_pub->execute($pub{authors},
                           $pub{title},
                           $pub{keywords},
                           $pub{pub_abstract},
                           $pub{pub_errata_and_notes},
                           $pub{pubmed_authors},
                           $pub{pub_mini_ref},
                           $pub{pub_authors_lower},
                           $pub{zdb_id});
      print PUBS "$pub{zdb_id}\n";
    }
  };
  print ERRS "$pub{zdb_id}\n" if $@;
}

$all_pubs->finish();
$update_pub->finish();
$dbh->disconnect();

close(ERRS);
close(PUBS);

# exit with error code if unreplaceable characters were found
exit 1 if -s "fixCharsErrors.txt";

sub do_replacement {
  # decode the incoming octets as UTF-8
  $unichar = decode('UTF-8', $_[0]);

  # don't include unicode replacement char or funky private use area chars
  die "Unreplaceable character found -- please fix pub manually" if $unichar eq "\x{FFFD}" ||
                                                                    !charnames::viacode(ord($unichar));

  # encode the resulting character with ISO-8859-1 (what Informix is expecting)
  # and use the HTML character reference fallback for out-of-range characters
  return encode("iso-8859-1", $unichar, Encode::FB_HTMLCREF);
}
