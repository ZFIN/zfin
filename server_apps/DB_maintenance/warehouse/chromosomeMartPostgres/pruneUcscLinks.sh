#!/bin/bash
# Fail on error and propagate failures in pipelines
set -eo pipefail

# Prune UCSCStartEndLoader rows from sequence_feature_chromosome_location_generated
# whose accession is not present in UCSC's danRer11 refGene track. UCSC's
# hgTracks returns 404 for accessions it doesn't carry, so we drop those rows
# to avoid broken links on the mapping detail page. Runs after
# updateSequenceFeatureChromosomeLocationPostgres.sql in regenChromosomeMart.sh.

# Timestamped tracing for easier correlation in CI logs
PS4='+[$(date +"%Y-%m-%d %H:%M:%S")][$LINENO] '
set -x

CHROMOSOMEMARTDIR="$ROOT_PATH/server_apps/DB_maintenance/warehouse/chromosomeMartPostgres"
TSV_FILE="$CHROMOSOMEMARTDIR/ucscRefGeneAccessions.tsv"
API_URL="https://api.genome.ucsc.edu/getData/track?genome=danRer11&track=refGene"
MIN_ACCESSIONS=10000  # observed ~16k; bail if a transient response truncates the list
FETCH_RETRY_DELAYS=(60 300 600)  # escalating waits between fetch attempts: 1m, 5m, 10m

rm -f "$TSV_FILE"

TMP_JSON="$(mktemp)"

echo "fetching UCSC danRer11 refGene track from $API_URL"
attempt=1
max_attempts=$(( ${#FETCH_RETRY_DELAYS[@]} + 1 ))
until curl -sf --max-time 120 "$API_URL" -o "$TMP_JSON"; do
  if [ "$attempt" -ge "$max_attempts" ]; then
    echo "ERROR: failed to fetch UCSC refGene track after $max_attempts attempts"
    exit 1
  fi
  delay="${FETCH_RETRY_DELAYS[$((attempt - 1))]}"
  echo "  fetch attempt $attempt/$max_attempts failed; retrying in ${delay}s"
  attempt=$((attempt + 1))
  sleep "$delay"
done
echo "  downloaded $(wc -c < "$TMP_JSON") bytes"

jq -r '[.refGene[][].name] | unique | .[]' "$TMP_JSON" > "$TSV_FILE"
count=$(wc -l < "$TSV_FILE" | tr -d ' ')
echo "  extracted $count distinct accessions to $TSV_FILE"

if [ "$count" -lt "$MIN_ACCESSIONS" ]; then
  echo "ERROR: only $count accessions extracted (< $MIN_ACCESSIONS); refusing to prune"
  exit 1
fi

echo "pruning UCSCStartEndLoader rows missing from refGene allowlist"
psql -v ON_ERROR_STOP=1 "$DB_NAME" <<SQL
begin;

create temp table tmp_ucsc_refgene_accessions (accnum varchar(50) primary key);
\copy tmp_ucsc_refgene_accessions (accnum) from '$TSV_FILE'

select count(*) as ucsc_refgene_accessions from tmp_ucsc_refgene_accessions;

select count(*) as ucsc_rows_before
  from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'UCSCStartEndLoader';

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'UCSCStartEndLoader'
   and sfclg_acc_num not in (select accnum from tmp_ucsc_refgene_accessions);

select count(*) as ucsc_rows_after
  from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'UCSCStartEndLoader';

commit;
SQL

echo "UCSC link prune complete"
