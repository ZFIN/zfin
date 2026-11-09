#!/bin/bash
#
# Re-fetch from PubMed the publications of the last N months that still have an
# empty volume or pages, or whose pages are an Elsevier-style PII placeholder
# (S0000-0000(26)00000-0, stored before the loader learned to skip them), and
# fill them in (fetchMode=update never overwrites a populated value with a blank, and
# authors are left alone unless asked, see below).
#
# The window is the last 3 months, widened to 24 months on the 3rd of each month
# as a longer sweep. Pass a number of months to override it, e.g. for the one-time
# catch-up after ZFIN-10241 is deployed (the Jenkins job has a MONTHS parameter):
#   ./updatePubPages.sh 240
#
# Usage: ./updatePubPages.sh [months]

set -euo pipefail

MONTHS=${1:-$(( 10#$(date +%d) == 3 ? 24 : 3 ))}
[[ "$MONTHS" =~ ^[0-9]+$ ]] || { echo "months must be a number: $MONTHS" >&2; exit 1; }
UPDATE_AUTHORS=${2:-false}
[[ "$UPDATE_AUTHORS" =~ ^(true|false)$ ]] || { echo "updateAuthors must be true or false: $UPDATE_AUTHORS" >&2; exit 1; }

cd "$(dirname "$0")"

PMIDS=$(mktemp "${TMPDIR:-/tmp}/pubsMissingPages.XXXXXX")
trap 'rm -f "$PMIDS"' EXIT

ELSEVIER_PII_REGEX='^S[0-9]{4}-[0-9]{3}[0-9Xx]\('

MISSING_SQL="FROM publication
   WHERE accession_no IS NOT NULL
     AND (pub_pages IS NULL OR pub_pages = '' OR pub_pages ~ '$ELSEVIER_PII_REGEX' OR pub_volume IS NULL OR pub_volume = '')
     AND pub_date >= now() - interval '$MONTHS months'"

psql -At -d "$DBNAME" -c "SELECT accession_no $MISSING_SQL ORDER BY pub_date DESC" > "$PMIDS"

echo "$(wc -l < "$PMIDS") publications missing volume/pages (last $MONTHS months)"

if [ -s "$PMIDS" ]; then
  xargs -n 200 -a "$PMIDS" \
    groovy -cp "$GROOVY_CLASSPATH:." -DfetchMode=update -DupdateAuthors=$UPDATE_AUTHORS ./fetchPubsFromPubMed.groovy
fi

psql -At -d "$DBNAME" -c "SELECT count(*) || ' still missing volume/pages' $MISSING_SQL"
