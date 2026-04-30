#!/usr/bin/env bash
# Trigger a Solr replication backup and wait for it to complete.
#
# Solr's /replication?command=backup is asynchronous; status surfaces
# under /replication?command=details as details.backup.status. We poll
# until success/failed or until TIMEOUT seconds elapse.
#
# Usage (env-driven):
#   SOLR=http://solr:8983/solr CORE=site_index \
#   LOCATION=/opt/zfin/unloads/solr/${INSTANCE} NAME=2026.04.29-15.30 \
#   ./solr-backup.sh
#
# LOCATION must be under a path Solr is allowed to write to (see the
# solr.allowPaths sysprop on the solr service in docker-compose.yml).
# The snapshot lands at ${LOCATION}/snapshot.${NAME}/.

set -euo pipefail

SOLR="${SOLR:-http://solr:8983/solr}"
CORE="${CORE:-site_index}"
LOCATION="${LOCATION:?must set LOCATION}"
NAME="${NAME:?must set NAME}"
TIMEOUT="${TIMEOUT:-3600}"

mkdir -p "${LOCATION}"

curl -fsS -G \
  --data-urlencode "command=backup" \
  --data-urlencode "location=${LOCATION}" \
  --data-urlencode "name=${NAME}" \
  "${SOLR}/${CORE}/replication" >/dev/null
echo "backup triggered: ${LOCATION}/snapshot.${NAME}"

deadline=$(( $(date +%s) + TIMEOUT ))
while (( $(date +%s) < deadline )); do
  resp=$(curl -fsS -G --data-urlencode "command=details" "${SOLR}/${CORE}/replication?wt=xml" || true)
  # Extract just the <lst name="backup">...</lst> block, then pull status + snapshotName
  blk=$(printf '%s' "$resp" | tr -d '\n' | sed -n 's|.*<lst name="backup">\(.*\)</lst>.*|\1|p')
  st=$(printf '%s' "$blk" | sed -n 's|.*<str name="status">\([^<]*\)</str>.*|\1|p')
  sn=$(printf '%s' "$blk" | sed -n 's|.*<str name="snapshotName">\([^<]*\)</str>.*|\1|p')
  if [ "$sn" = "$NAME" ] && [ "$st" = "success" ]; then
    echo "backup complete: ${LOCATION}/snapshot.${NAME}"
    exit 0
  fi
  if [ "$sn" = "$NAME" ] && [ "$st" = "failed" ]; then
    echo "backup failed for ${NAME}" >&2
    exit 1
  fi
  sleep 5
done

echo "backup timed out after ${TIMEOUT}s" >&2
exit 1
