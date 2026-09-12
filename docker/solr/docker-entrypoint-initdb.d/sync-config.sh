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

# The configset the reindex creates its staging core from. conf/ and lib/ both:
# solrconfig.xml declares no <lib> directive, so the DIH jar and the JDBC
# driver are picked up from the core's own lib/, and a core created from a
# configset only gets what the configset carries.
echo "[zfin-init] Materialising configset at $CONFIGSET"
mkdir -p "$CONFIGSET/conf" "$CONFIGSET/lib"
cp -a "$TEMPLATE/conf"/. "$CONFIGSET/conf/"
cp -a "$TEMPLATE/lib"/.  "$CONFIGSET/lib/"

# external_popularity.txt is an image-owned ExternalFileField asset that
# happens to live under data/ alongside the index. Refresh it; the three
# index subdirs (index/, tlog/, snapshot_metadata/) are skipped because we
# only touch this one file explicitly.
if [ -f "$TEMPLATE/data/external_popularity.txt" ]; then
  cp -a "$TEMPLATE/data/external_popularity.txt" "$CORE/data/"
fi

# log4j2.xml lives at SOLR_HOME's parent, not inside the core.
cp -a /opt/zfin-solr/template/log4j2.xml /var/solr/log4j2.xml

echo "[zfin-init] sync complete"
