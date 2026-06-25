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

echo "[zfin-init] Syncing site_index from $TEMPLATE -> $CORE"

mkdir -p "$CORE/conf" "$CORE/lib" \
         "$CORE/data/index" "$CORE/data/tlog" "$CORE/data/snapshot_metadata"

# conf/, lib/, and core.properties are always refreshed from the image.
# Using rsync-style 'cp -a SRC/. DEST/' so existing files in DEST that are
# absent from SRC are NOT removed — DIH's runtime dataimport.properties lives
# at $CORE/dataimport.properties (NOT in conf/), per the <propertyWriter>
# element in db-data-config.xml, so this sync can't accidentally trample it.
cp -a "$TEMPLATE/conf"/. "$CORE/conf/"
cp -a "$TEMPLATE/lib"/.  "$CORE/lib/"
cp -a "$TEMPLATE/core.properties" "$CORE/core.properties"

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
