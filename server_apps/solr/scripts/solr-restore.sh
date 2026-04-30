#!/usr/bin/env bash
# Trigger a Solr replication restore and wait for it to complete.
#
# Solr's /replication?command=restore is asynchronous; status surfaces
# under /replication?command=restorestatus. We poll until success/failed
# or until TIMEOUT seconds elapse.
#
# Usage (env-driven):
#   SOLR=http://solr:8983/solr CORE=site_index \
#   LOCATION=/opt/zfin/unloads/solr/${INSTANCE} NAME=2026.04.29-15.30 \
#   ./solr-restore.sh
#
# Reads from ${LOCATION}/snapshot.${NAME}/. The restore replaces the
# core's index in place; the core remains running.

set -euo pipefail

SOLR="${SOLR:-http://solr:8983/solr}"
CORE="${CORE:-site_index}"
LOCATION="${LOCATION:?must set LOCATION}"
NAME="${NAME:?must set NAME}"
TIMEOUT="${TIMEOUT:-3600}"

curl -fsS -G \
  --data-urlencode "command=restore" \
  --data-urlencode "location=${LOCATION}" \
  --data-urlencode "name=${NAME}" \
  "${SOLR}/${CORE}/replication" >/dev/null
echo "restore triggered: ${LOCATION}/snapshot.${NAME}"

deadline=$(( $(date +%s) + TIMEOUT ))
while (( $(date +%s) < deadline )); do
  resp=$(curl -fsS "${SOLR}/${CORE}/replication?command=restorestatus&wt=xml" || true)
  blk=$(printf '%s' "$resp" | tr -d '\n' | sed -n 's|.*<lst name="restorestatus">\(.*\)</lst>.*|\1|p')
  st=$(printf '%s' "$blk" | sed -n 's|.*<str name="status">\([^<]*\)</str>.*|\1|p')
  sn=$(printf '%s' "$blk" | sed -n 's|.*<str name="snapshotName">\([^<]*\)</str>.*|\1|p')
  # Solr reports snapshotName as "snapshot.<NAME>"
  if [ "$sn" = "snapshot.${NAME}" ] && [ "$st" = "success" ]; then
    echo "restore complete from snapshot.${NAME}"
    exit 0
  fi
  if [ "$sn" = "snapshot.${NAME}" ] && [ "$st" = "failed" ]; then
    echo "restore failed for snapshot.${NAME}" >&2
    exit 1
  fi
  sleep 5
done

echo "restore timed out after ${TIMEOUT}s" >&2
exit 1
