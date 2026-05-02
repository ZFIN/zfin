#!/bin/bash

# Interactive ZFIN release driver. Walks through deployment steps with
# forward/back navigation so you can re-run a step without restarting.
# Auto-wraps in a screen session and a `script` typescript recording.
# Set NO_SCREEN=1 or NO_SCRIPT=1 to opt out of either wrapper
# (e.g. on macOS where `script` flags differ).

cmprun() {
    docker compose run --rm compile bash -lc "$1"
}

SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/$(basename "${BASH_SOURCE[0]}")"
LOG_DIR=/research/zusers/informix/release-logs

if [ -z "$RELEASE_PROMPTS_INPUTS" ]; then
    read -rp "Release number (e.g. 1234): " RELEASE
    read -rp "Deploy dir (e.g. /opt/zfin/source_roots/test/zfin/docker): " DEPLOY_DIR

    if [ -z "$RELEASE" ] || [ -z "$DEPLOY_DIR" ]; then
        echo "Both release number and deploy dir are required."
        exit 1
    fi

    export RELEASE DEPLOY_DIR
    export RELEASE_PROMPTS_INPUTS=1
fi

if [ -z "$STY" ] && [ -z "$NO_SCREEN" ]; then
    if ! command -v screen >/dev/null; then
        echo "screen not found. Install it or rerun with NO_SCREEN=1." >&2
        exit 1
    fi
    echo "Launching screen session 'release-$RELEASE' (detach with Ctrl-A d)..."
    exec screen -S "release-$RELEASE" "$SCRIPT_PATH"
fi

if [ -z "$RELEASE_PROMPTS_RECORDING" ] && [ -z "$NO_SCRIPT" ]; then
    if ! command -v script >/dev/null; then
        echo "script not found. Install util-linux or rerun with NO_SCRIPT=1." >&2
        exit 1
    fi
    mkdir -p "$LOG_DIR"
    export RELEASE_PROMPTS_RECORDING=1
    echo "Recording session to $LOG_DIR/$RELEASE (timing $LOG_DIR/$RELEASE.timing)..."
    exec script --timing="$LOG_DIR/$RELEASE.timing" "$LOG_DIR/$RELEASE" -c "$SCRIPT_PATH"
fi

labels=(
    "cd $DEPLOY_DIR"
    "docker compose down jenkins"
    "cmprun 'git status'"
    "cmprun 'git fetch'"
    "cmprun 'git checkout release-$RELEASE'"
    "cmprun 'git log' (compare to TEST)"
    "sed -i 's/RELEASE=[0-9]*/RELEASE=$RELEASE/' .env"
    "docker compose pull"
    "cmprun 'gradle liquibasePreBuild'"
    "cmprun 'gradle make'"
    "cmprun 'gradle liquibasePostBuild'"
    "cmprun 'ant deploy-catalina-base'"
    "cmprun 'ant deploy'"
    "cmprun 'ant deploy-jobs'"
    "cmprun 'ant deploy-plugins'"
    "cmprun 'ant create-views'"
    "docker compose up -d jenkins"
    "docker compose down httpd"
    "docker compose up -d httpd"
    "docker compose down db"
    "docker compose up -d db"
    "docker compose down tomcat"
    "docker compose up -d tomcat"
    "docker compose down solr"
    "docker compose up -d solr"
)

commands=(
    "cd \"$DEPLOY_DIR\""
    "docker compose down jenkins"
    "cmprun 'git status'"
    "cmprun 'git fetch'"
    "cmprun \"git checkout release-$RELEASE\""
    "cmprun 'git log'"
    "sed -i 's/RELEASE=[0-9]*/RELEASE=$RELEASE/' .env"
    "docker compose pull"
    "cmprun 'gradle liquibasePreBuild'"
    "cmprun 'gradle make'"
    "cmprun 'gradle liquibasePostBuild'"
    "cmprun 'ant deploy-catalina-base'"
    "cmprun 'ant deploy'"
    "cmprun 'ant deploy-jobs'"
    "cmprun 'ant deploy-plugins'"
    "cmprun 'ant create-views'"
    "docker compose up -d jenkins"
    "docker compose down httpd"
    "docker compose up -d httpd"
    "docker compose down db"
    "docker compose up -d db"
    "docker compose down tomcat"
    "docker compose up -d tomcat"
    "docker compose down solr"
    "docker compose up -d solr"
)

total=${#labels[@]}
i=0

while [ "$i" -lt "$total" ]; do
    echo
    echo "[Step $((i + 1))/$total] ${labels[$i]}"
    read -rp "Action? (Y/Enter=run, S=skip, B=back, F=forward without running, Q=quit): " choice

    case "$choice" in
        ""|[Yy]*)
            if [ -n "${commands[$i]}" ]; then
                eval "${commands[$i]}"
            fi
            i=$((i + 1))
            ;;
        [Ss]*|[Ff]*)
            i=$((i + 1))
            ;;
        [Bb]*)
            if [ "$i" -gt 0 ]; then
                i=$((i - 1))
            else
                echo "Already at the first step."
            fi
            ;;
        [Qq]*)
            echo "Quitting."
            exit 0
            ;;
        *)
            echo "Please answer Y, S, B, F, or Q."
            ;;
    esac
done

echo
echo "All steps complete."
