#!/bin/bash
# Sync image-baked Solr config into /var/solr so the volume can never serve
# stale config/lib/log4j. Index data, tlog, and snapshots are intentionally
# left alone — those are the only paths the volume legitimately persists.
#
# Runs from the upstream Solr image's docker-entrypoint-initdb.d/ hook, which
# executes before solr starts on every container launch. See
# https://solr.apache.org/guide/solr/latest/deployment-guide/solr-in-docker.html
# (the docker image's documented extension point).

set -e

TEMPLATE=/opt/solr/server/solr/configsets/site_index
CORE=/var/solr/data/site_index
# The reindex builds here and publishes by swapping names with $CORE.
STAGING=/var/solr/data/site_index_staging
# Solr resolves configSet= against $SOLR_HOME/configsets, and SOLR_HOME is
# /var/solr/data. The path above is only the image-side template Solr never
# reads directly, so the reindex's CoreAdmin CREATE needs a real configset
# materialised here (ZFIN-10497).
CONFIGSET=/var/solr/data/configsets/site_index

echo "[zfin-init] Syncing site_index from $TEMPLATE -> $CORE"

mkdir -p "$CORE/conf" "$CORE/lib" \
         "$CORE/data/index" "$CORE/data/tlog" "$CORE/data/snapshot_metadata"

# conf/ and lib/ are always refreshed from the image.
# Using rsync-style 'cp -a SRC/. DEST/' so existing files in DEST that are
# absent from SRC are NOT removed — DIH's runtime dataimport.properties lives
# at $CORE/dataimport.properties (NOT in conf/), per the <propertyWriter>
# element in db-data-config.xml, so this sync can't accidentally trample it.
cp -a "$TEMPLATE/conf"/. "$CORE/conf/"
cp -a "$TEMPLATE/lib"/.  "$CORE/lib/"

# core.properties is written only when absent, unlike conf/ and lib/.
#
# It carries the core's NAME, and the reindex publishes by swapping names
# between this directory and $STAGING (ZFIN-10497) — Solr persists that swap
# by rewriting core.properties in both. Re-copying the template afterwards
# would reset this directory to name=site_index while the swapped directory
# still claims it, leaving two cores with one name and Solr refusing to load
# one of them on the next start. The breakage would surface at a restart,
# detached from the reindex that caused it.
#
# Nothing else in the file changes between image builds, so "create once" is
# not a loss. If it ever needs to change, delete it and let this recreate it.
if [ ! -f "$CORE/core.properties" ]; then
  cp -a "$TEMPLATE/core.properties" "$CORE/core.properties"
fi

# After a swap the live index lives in $STAGING, so it needs current conf/lib
# just as much as $CORE does. Only if it exists — it is created by the reindex,
# not here, and is absent before the first run.
if [ -d "$STAGING" ]; then
  echo "[zfin-init] Refreshing swapped-aside core at $STAGING"
  mkdir -p "$STAGING/conf" "$STAGING/lib"
  cp -a "$TEMPLATE/conf"/. "$STAGING/conf/"
  cp -a "$TEMPLATE/lib"/.  "$STAGING/lib/"
fi

# The configset the reindex creates its staging core from. conf/, lib/ AND
# data/: solrconfig.xml declares no <lib> directive, so the DIH jar and the
# JDBC driver are picked up from the core's own lib/, and a core created from a
# configset only gets what the configset carries.
echo "[zfin-init] Materialising configset at $CONFIGSET"
mkdir -p "$CONFIGSET/conf" "$CONFIGSET/lib" "$CONFIGSET/data"
cp -a "$TEMPLATE/conf"/. "$CONFIGSET/conf/"
cp -a "$TEMPLATE/lib"/.  "$CONFIGSET/lib/"

# external_popularity.txt is an image-owned ExternalFileField asset that lives
# under data/ alongside the index, keyed by document id.
#
# It matters more than its size suggests. The /name-autocomplete handler ranks
# on "bf=recip(complexity,1,1,1)^10 sqrt(popularity)^20", and popularity is a
# solr.ExternalFileField read from this file. With the file missing every
# document falls back to defVal=1, the dominant boost goes flat, and the
# ranking collapses onto complexity plus raw field matches -- an exact gene
# match then loses to Fish records (ZFIN-10514).
#
# Placed in three spots, because the reindex's staging/swap means no single
# directory is reliably the live core:
#
#  1. The configset, so a core the reindex CREATEs inherits it. This is the
#     only one that can survive mid-run: prepareStagingCore unloads the parked
#     core with deleteInstanceDir=true and CREATEs a fresh one, all long after
#     this script has run, so a copy placed only in a core directory is
#     destroyed on every publish. See the caveat below.
#  2. and 3. Both conventional core directories, when they exist. A SWAP
#     exchanges names and leaves directories in place, so after one publish the
#     live core is the directory named site_index_staging. Copying into both
#     costs 11MB and removes the guesswork.
#
# CAVEAT, not yet verified on a running instance: whether Solr's CoreAdmin
# CREATE copies a configset's data/ into the new instanceDir, or only resolves
# its conf/. If it only resolves conf/, item 1 does nothing and a core created
# mid-run still starts without the file. Check after the next reindex with
#   ls -l /var/solr/data/*/data/external_popularity.txt
# and if the freshly created staging core lacks it, this needs solving in the
# orchestrator instead -- it cannot write into the Solr container over HTTP, so
# the likely answer is to stop deleting the parked instance directory.
if [ -f "$TEMPLATE/data/external_popularity.txt" ]; then
  cp -a "$TEMPLATE/data/external_popularity.txt" "$CONFIGSET/data/"
  for d in "$CORE" "$STAGING"; do
    if [ -d "$d" ]; then
      mkdir -p "$d/data"
      cp -a "$TEMPLATE/data/external_popularity.txt" "$d/data/"
      echo "[zfin-init] Placed external_popularity.txt in $d/data"
    fi
  done
else
  echo "[zfin-init] WARNING: no external_popularity.txt in the image template;" \
       "autocomplete ranking will be flat (see ZFIN-10514)"
fi

# log4j2.xml lives at SOLR_HOME's parent, not inside the core.
cp -a /opt/zfin-solr/template/log4j2.xml /var/solr/log4j2.xml

echo "[zfin-init] sync complete"
