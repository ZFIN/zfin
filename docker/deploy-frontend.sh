#!/usr/bin/env bash
#
# Deploy the webpack output to the local dev stack.
#
# The /dist/* URLs are NOT served by Tomcat: Apache serves them from a Docker
# volume, while asset-manifest.json (which the JSPs read to emit <script> URLs)
# lives in TWO places that must both be updated:
#
#   www_homes/<host>/home/asset-manifest.json   <- the one getAssetPath() reads
#   catalina_base/.../WEB-INF/classes/asset-manifest.json
#
# Updating only the classpath copy leaves the server emitting the PREVIOUS
# build's bundle URLs. Combined with clearing dist that yields a page with no
# JavaScript at all: the HTML references a file that no longer exists. That
# happened during ZFIN-10407 and took a long time to find, because the stale
# URL is in the server's own HTML and looks like a browser cache problem.
#
# Why superseded bundles are removed:
#   Copying over the top leaves every previous build's hashed chunks in place.
#   That is not merely untidy — it lets a client holding a stale
#   zfin-common.latest.js (which carries webpack's runtime chunk map) keep
#   resolving old chunk names that are still on disk and still served 200.
#   ZFIN-10407 was chased for hours with three generations of one chunk present
#   and the browser loading the oldest. Removing superseded hashed bundles makes
#   such a client fail loudly (404) instead of silently running old code.
#
#   Only files whose names carry a content hash are removed, and only when the
#   new build does not also produce them. Anything else is kept and listed.
#
# Copies are tar-piped out of the compile container rather than `docker cp`d
# from the host, so files keep container ownership (1000:1000). Host-owned
# files in these volumes break the next dirtycopy with EPERM.
#
# Usage:  docker/deploy-frontend.sh [--no-clear]
#
#   --no-clear   keep the existing dist contents (use only if you deliberately
#                need old bundles to stay resolvable, e.g. for a client you
#                cannot reload)
#
# Run the build first, with the full env preamble -- PRIMARY_COLOR is required
# or Bootstrap emits `null` for $primary and the site's CSS breaks:
#
#   docker exec <compile> bash -lc 'cd $SOURCEROOT && \
#     TARGETROOT=$(pwd)/target NODE_ENV=development WIKI_HOST=localhost \
#     ZFIN_ADMIN=admin PRIMARY_COLOR="#1976d2" npm run compile'

set -euo pipefail

CLEAR_DIST=1
[[ "${1:-}" == "--no-clear" ]] && CLEAR_DIST=0

SOURCE_ROOT="${SOURCE_ROOT:-/opt/zfin/source_roots/zfin.org}"
HTTPD_DIST="${HTTPD_DIST:-/opt/zfin/www_homes/zfin.org/home/dist}"
TOMCAT_CLASSES="${TOMCAT_CLASSES:-/opt/zfin/catalina_bases/zfin.org/webapps/ROOT/WEB-INF/classes}"

find_container() {
    local name
    name=$(docker ps --filter "name=$1" --format '{{.Names}}' | head -1)
    [[ -n "$name" ]] || { echo "error: no running container matching '$1'" >&2; exit 1; }
    echo "$name"
}

COMPILE=$(find_container "${COMPILE_CONTAINER:-compile}")
HTTPD=$(find_container   "${HTTPD_CONTAINER:-httpd}")
TOMCAT=$(find_container  "${TOMCAT_CONTAINER:-tomcatdebug}")

echo "compile: $COMPILE"
echo "httpd:   $HTTPD"
echo "tomcat:  $TOMCAT"

# Fail early rather than deploying half a build.
docker exec "$COMPILE" test -d "$SOURCE_ROOT/target/home/dist" \
    || { echo "error: no build output at $SOURCE_ROOT/target/home/dist -- run npm run compile first" >&2; exit 1; }
docker exec "$COMPILE" test -f "$SOURCE_ROOT/target/home/asset-manifest.json" \
    || { echo "error: no asset-manifest.json in the build output" >&2; exit 1; }

if (( CLEAR_DIST )); then
    # Delete ONLY superseded content-hashed webpack output: a name carrying a
    # >=16 hex-char hash segment that the new build does not also produce.
    #
    # A blanket "rm -rf dist/*" is wrong. That volume has also held files no
    # local build reproduces (vendored libraries, zfin-static release content),
    # and deleting those leaves nothing to restore them from.
    docker exec "$COMPILE" sh -c "cd '$SOURCE_ROOT/target/home/dist' && ls -1" > /tmp/_new_dist.txt
    docker exec "$HTTPD" sh -c "ls -1 '$HTTPD_DIST' 2>/dev/null" > /tmp/_old_dist.txt
    stale=$(grep -vxF -f /tmp/_new_dist.txt /tmp/_old_dist.txt 2>/dev/null \
            | grep -E '\.[0-9a-f]{16,}\.(js|css|map)$' || true)
    kept=$(grep -vxF -f /tmp/_new_dist.txt /tmp/_old_dist.txt 2>/dev/null \
            | grep -vE '\.[0-9a-f]{16,}\.(js|css|map)$' || true)
    if [[ -n "$stale" ]]; then
        echo "removing $(echo "$stale" | wc -l | tr -d ' ') superseded hashed bundles"
        echo "$stale" | while read -r f; do
            [[ -n "$f" ]] && docker exec "$HTTPD" sh -c "rm -f '$HTTPD_DIST/$f'"
        done
    else
        echo "no superseded hashed bundles to remove"
    fi
    if [[ -n "$kept" ]]; then
        echo "keeping $(echo "$kept" | wc -l | tr -d ' ') file(s) not produced by this build:"
        echo "$kept" | sed 's/^/    /' | head -10
    fi
    rm -f /tmp/_new_dist.txt /tmp/_old_dist.txt
else
    echo "keeping all existing dist contents (--no-clear)"
fi

echo "copying dist -> $HTTPD:$HTTPD_DIST"
docker exec "$COMPILE" sh -c "cd '$SOURCE_ROOT/target/home' && tar c dist" \
    | docker exec -i "$HTTPD" tar x -C "$(dirname "$HTTPD_DIST")"

echo "copying asset-manifest.json -> $HTTPD:$(dirname "$HTTPD_DIST") (the one getAssetPath reads)"
docker exec "$COMPILE" sh -c "cd '$SOURCE_ROOT/target/home' && tar c asset-manifest.json" \
    | docker exec -i "$HTTPD" tar x -C "$(dirname "$HTTPD_DIST")"

echo "copying asset-manifest.json -> $TOMCAT:$TOMCAT_CLASSES"
docker exec "$COMPILE" sh -c "cd '$SOURCE_ROOT/target/home' && tar c asset-manifest.json" \
    | docker exec -i "$TOMCAT" tar x -C "$TOMCAT_CLASSES"

after=$(docker exec "$HTTPD" sh -c "ls '$HTTPD_DIST' 2>/dev/null | wc -l" | tr -d ' \r')
echo "done: $after files in dist"

# The manifest is what the JSPs read; if it disagrees with dist, pages request
# bundles that are not there.
manifest="$(dirname "$HTTPD_DIST")/asset-manifest.json"
entry=$(docker exec "$HTTPD" sh -c "grep -oE '\"react\.js\": \"[^\"]*\"' '$manifest'" || true)
echo "manifest react.js -> ${entry:-<not found>}"

# The whole failure mode is the manifest naming a bundle that is not on disk.
target=$(echo "$entry" | grep -oE '/dist/[^\"]+' || true)
if [[ -n "$target" ]]; then
    if docker exec "$HTTPD" sh -c "test -f '$(dirname "$HTTPD_DIST")$target'"; then
        echo "verified: $target exists"
    else
        echo "ERROR: manifest points at $target which is NOT deployed" >&2
        exit 1
    fi
fi
