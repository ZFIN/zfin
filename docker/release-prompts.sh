#!/bin/bash

# Interactive ZFIN release driver. Walks through deployment steps with
# forward/back navigation so you can re-run a step without restarting.
# Auto-wraps in a screen session and a `script` typescript recording.
# Set NO_SCREEN=1 or NO_SCRIPT=1 to opt out of either wrapper
# (e.g. on macOS where `script` flags differ).

# The docker compose steps need an ssh agent socket to hand to the containers.
# Check before doing anything else.
if [ -z "$SSH_AUTH_SOCK" ]; then
    echo "SSH_AUTH_SOCK is not set; docker compose needs it." >&2
    echo "  Start an agent with:   eval \`ssh-agent -s\`" >&2
    echo "  or, if you don't need ssh inside the container, a bogus value will do:" >&2
    echo "    export SSH_AUTH_SOCK=/tmp/auth.txt" >&2
    exit 1
fi

cmprun() {
    docker compose run --rm compile bash -lc "$1"
}

# Reminders for things that have to be done by hand. Waits for Enter so the
# step can't scroll past unread.
manual() {
    echo
    echo "  >> MANUAL: $1"
    read -rp "  Press Enter once done (or if not needed)... " _
}

# Drops tomcat's open connections before the build touches the database.
# DBNAME comes from the container's login environment, hence the escaped \$.
kill_web_connections() {
    cmprun "psql -d \"\$DBNAME\" -c \"select pg_terminate_backend(pid) from pg_stat_activity where application_name = 'PostgreSQL JDBC Driver';\""
}

# `gradle loaddb` drops and reloads the database, so it must never run
# against production. ENVIRONMENT in the instance's zfin.properties is the
# authoritative marker; the hostname and the bound address are backstops for
# when that file is missing or hasn't been written yet. Any one of the three
# is enough to refuse.
PROD_ENVIRONMENT=production
PROD_HOSTNAME=franklin.zfin.org
PROD_IP=184.171.92.30

this_hostname() {
    hostname -f 2>/dev/null || hostname 2>/dev/null || echo unknown
}

zfin_properties_path() {
    echo "$DEPLOY_DIR/../home/WEB-INF/zfin.properties"
}

# Value of ENVIRONMENT, or empty if the file is absent or has no such key.
# Last assignment wins, as it would when java loads the file.
zfin_environment() {
    local props
    props="$(zfin_properties_path)"
    [ -r "$props" ] || return 0
    sed -n 's/^[[:space:]]*ENVIRONMENT[[:space:]]*=[[:space:]]*\([^[:space:]]*\).*/\1/p' \
        "$props" | tail -n 1
}

# hostname -I is GNU-only; ip and ifconfig cover the rest. -F with -x/-w so
# the dots aren't read as regex wildcards and so a longer address that merely
# starts with these digits can't match.
host_has_ip() {
    hostname -I 2>/dev/null | tr ' ' '\n' | grep -qxF "$1" && return 0
    ip -o addr show 2>/dev/null | grep -qwF "$1" && return 0
    ifconfig -a 2>/dev/null | grep -qwF "$1" && return 0
    return 1
}

# Sets PROD_REASON so the refusal can say which signal tripped.
PROD_REASON=""
is_production() {
    PROD_REASON=""

    if [ "$(zfin_environment)" = "$PROD_ENVIRONMENT" ]; then
        PROD_REASON="ENVIRONMENT=$PROD_ENVIRONMENT in $(zfin_properties_path)"
        return 0
    fi

    case "$(this_hostname)" in
        "$PROD_HOSTNAME"|franklin)
            PROD_REASON="hostname is $(this_hostname)"
            return 0
            ;;
    esac

    if host_has_ip "$PROD_IP"; then
        PROD_REASON="$PROD_IP is bound to this host"
        return 0
    fi

    return 1
}

loaddb() {
    if is_production; then
        echo
        echo "  !! REFUSING to run 'gradle loaddb': this looks like production." >&2
        echo "  !!   $PROD_REASON" >&2
        echo "  !! It drops and reloads the database. Nothing was run." >&2
        echo
        return 1
    fi
    cmprun 'gradle loaddb'
}

# Steps that default to NO: Enter skips them and you must type Y to run.
# Matched on the commands[] entry, so reordering steps can't misalign this.
step_defaults_to_no() {
    case "$1" in
        loaddb|kill_web_connections|"cmprun 'ant create-views'") return 0 ;;
        *) return 1 ;;
    esac
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_PATH="$SCRIPT_DIR/$(basename "${BASH_SOURCE[0]}")"
LOG_DIR=${LOG_DIR:-/research/zusers/informix/release-logs}

# One-time splash on the very first invocation. The screen/script re-execs
# each set one of these markers (STY / RELEASE_PROMPTS_RECORDING), so this
# block is skipped on those re-runs and the banner shows exactly once.
if [ -z "$STY" ] && [ -z "$RELEASE_PROMPTS_RECORDING" ]; then
    clear
    cat <<'BANNER'
================================================================
            ZFIN Release Deployment Driver
================================================================

Deployment wiki page:
  https://zfin.atlassian.net/wiki/spaces/systems/pages/6101467137

Steps through the release deployment one action at a time. At
each step you choose:

  Y / Enter   run it        S   skip it
  B           go back       F   forward without running
  Q           quit

Some steps default to NO -- there Enter skips, and you have to
type Y to run them.

The run auto-wraps in a `screen` session (detach: Ctrl-A d) and
is recorded with `script` under:
  /research/zusers/informix/release-logs/

These logs can be replayed via (for example):
  scriptreplay -d 3 -t ./1178.timing ./1178
The -d flag is the divisor that determines playback speed.

If you run into git permission issues, they are likely caused by
git commands running inside a container, leaving files owned by
the zfin user outside the container (gradle user inside) with
user id of 1000. This adds group write permission to those files:
  sudo find . -user zfin \! -perm -g=w -exec chmod g+w {} +

Environment knobs (all optional):

  RELEASE=<num>       release number    -- skips the prompt
  DEPLOY_DIR=<path>   deploy directory  -- skips the prompt
                      -- defaults to this script's own directory
  BEGIN_STEP=<n>      jump straight to step <n>
  NO_SCREEN=1         don't wrap in screen
  NO_SCRIPT=1         don't record with script
  LOG_DIR=<path>      where to write recordings
                      -- default /research/zusers/informix/release-logs

Example (no prompts, start at step 8):

  RELEASE=1234 \
  DEPLOY_DIR=/opt/zfin/source_roots/test/zfin/docker \
  BEGIN_STEP=8 \
  ./release-prompts.sh

================================================================

BANNER
    read -rp "Press Enter to begin (Ctrl-C to abort)... " _
fi

# Prompt only for whichever inputs weren't already supplied. Setting both
# RELEASE and DEPLOY_DIR non-empty skips the reads entirely. They're exported
# below so the screen/script re-execs (and docker compose) inherit them
# whether they came from the environment or the prompts.
if [ -z "$RELEASE" ]; then
    read -rp "Release number (e.g. 1234): " RELEASE

    if [ -z "$RELEASE" ]; then
        echo "A release number is required."
        exit 1
    fi
fi

# This script ships inside the deploy directory it drives, so its own
# location is the right default -- an empty answer accepts it.
if [ -z "$DEPLOY_DIR" ]; then
    read -rp "Deploy dir [$SCRIPT_DIR]: " DEPLOY_DIR
    DEPLOY_DIR="${DEPLOY_DIR:-$SCRIPT_DIR}"
fi

export RELEASE DEPLOY_DIR

if [ -z "$STY" ] && [ -z "$NO_SCREEN" ]; then
    if ! command -v screen >/dev/null; then
        echo "screen not found. Install it or rerun with NO_SCREEN=1." >&2
        exit 1
    fi
    echo "Launching screen session 'release-$RELEASE' (detach with Ctrl-A d)..."
    exec screen -S "release-$RELEASE" "$SCRIPT_PATH"
fi

# Owner of a path, for the warning below. stat's flags differ between
# GNU (Linux) and BSD (macOS); fall back to "unknown" if neither works.
path_owner() {
    stat -c '%U' "$1" 2>/dev/null || stat -f '%Su' "$1" 2>/dev/null || echo unknown
}

if [ -z "$RELEASE_PROMPTS_RECORDING" ] && [ -z "$NO_SCRIPT" ]; then
    if ! command -v script >/dev/null; then
        echo "script not found. Install util-linux or rerun with NO_SCRIPT=1." >&2
        exit 1
    fi

    # Preflight the recording paths. LOG_DIR lives on a network mount and is
    # shared between accounts, so a stale mount, a full disk, or a log file
    # left behind by another user all make `script` exit the instant it
    # starts. Under screen that kills the window before the error can be
    # read, so check up front and pause on anything unwritable.
    log_problem=""
    if ! mkdir -p "$LOG_DIR" 2>/dev/null; then
        log_problem="cannot create log directory $LOG_DIR"
    else
        for log_file in "$LOG_DIR/$RELEASE" "$LOG_DIR/$RELEASE.timing"; do
            if [ -e "$log_file" ]; then
                if [ ! -w "$log_file" ]; then
                    log_problem="$log_file exists but is not writable (owned by $(path_owner "$log_file"))"
                fi
            elif ! (: > "$log_file") 2>/dev/null; then
                log_problem="cannot create $log_file (is $LOG_DIR writable? is the mount up? is it full?)"
            else
                # Only a probe -- let `script` create it for real.
                rm -f "$log_file"
            fi
            [ -n "$log_problem" ] && break
        done
    fi

    if [ -n "$log_problem" ]; then
        echo
        echo "  !! Cannot record this session:" >&2
        echo "  !!   $log_problem" >&2
        echo "  !! Running as $(id -un)." >&2
        echo "  !!" >&2
        echo "  !! Fix the permissions, or rerun with LOG_DIR=<somewhere writable>," >&2
        echo "  !! or with NO_SCRIPT=1 to skip recording deliberately." >&2
        echo
        read -rp "Press Enter to continue WITHOUT recording (Ctrl-C to abort)... " _
    else
        export RELEASE_PROMPTS_RECORDING=1
        echo "Recording session to $LOG_DIR/$RELEASE (timing $LOG_DIR/$RELEASE.timing)..."
        exec script --timing="$LOG_DIR/$RELEASE.timing" "$LOG_DIR/$RELEASE" -c "$SCRIPT_PATH"
    fi
fi

labels=(
    "MANUAL: make sure this is the most up-to-date version of this script"
    "cd $DEPLOY_DIR"
    "MANUAL: set Jenkins URL to franklin.zfin.org/jobs (if needed)"
    "docker compose down jenkins"
    "cmprun 'git status'"
    "cmprun 'git fetch'"
    "cmprun 'git checkout release-$RELEASE'"
    "cmprun 'git log' (compare to TEST)"
    "sed -i 's/RELEASE=[0-9]*/RELEASE=$RELEASE/' .env"
    "docker compose pull"
    "cmprun 'gradle liquibasePreBuild'"
    "kill existing web connections (defaults to NO)"
    "cmprun 'gradle make'"
    "cmprun 'gradle loaddb' (optional, DESTRUCTIVE -- defaults to NO)"
    "cmprun 'gradle liquibasePostBuild'"
    "cmprun 'ant deploy-catalina-base'"
    "cmprun 'ant deploy-without-tests'"
    "cmprun 'ant deploy-jobs'"
    "cmprun 'ant deploy-plugins'"
    "cmprun 'ant create-views' (defaults to NO)"
    "docker compose up -d jenkins"
    "docker compose down httpd"
    "docker compose up -d httpd"
    "docker compose down db"
    "docker compose up -d db"
    "docker compose down tomcat"
    "docker compose up -d tomcat"
    "docker compose down solr"
    "docker compose up -d solr"
    "cmprun 'ant test' (deferred DB tests + smoke; rolls back, slow)"
    "MANUAL: revert Jenkins URL to zfin.org/jobs"
    "MANUAL: after IP switching, restart nginx-proxy + arping (if needed)"
)

commands=(
    "manual \"Make sure this is the most up-to-date version of this script. If you haven't already, quit (Ctrl-C), run 'git fetch && git checkout release-$RELEASE' in this checkout, then start this script again\""
    "cd \"$DEPLOY_DIR\""
    "manual \"Temporarily set the Jenkins URL (Manage Jenkins > System) to 'https://franklin.zfin.org/jobs' instead of 'https://zfin.org/jobs', if needed\""
    "docker compose down jenkins"
    "cmprun 'git status'"
    "cmprun 'git fetch'"
    "cmprun \"git checkout release-$RELEASE\""
    "cmprun 'git log'"
    "sed -i 's/RELEASE=[0-9]*/RELEASE=$RELEASE/' .env"
    "docker compose pull"
    "cmprun 'gradle liquibasePreBuild'"
    "kill_web_connections"
    "cmprun 'gradle make'"
    "loaddb"
    "cmprun 'gradle liquibasePostBuild'"
    "cmprun 'ant deploy-catalina-base'"
    "cmprun 'ant deploy-without-tests'"
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
    "cmprun 'ant test'"
    "manual \"Revert the Jenkins URL (Manage Jenkins > System) to 'https://zfin.org/jobs' if you changed it at the start\""
    "manual \"After IP switching you may need to run: sudo systemctl restart nginx-proxy && sudo arping -s 184.171.92.30 184.171.92.1\""
)

total=${#labels[@]}

# BEGIN_STEP (1-indexed, matching the "[Step N/total]" display) jumps the
# loop straight to that step instead of starting at the top. Inherited across
# the screen/script re-execs from the initial environment.
i=0
if [ -n "$BEGIN_STEP" ]; then
    if ! [[ "$BEGIN_STEP" =~ ^[0-9]+$ ]] || [ "$BEGIN_STEP" -lt 1 ] || [ "$BEGIN_STEP" -gt "$total" ]; then
        echo "BEGIN_STEP must be a whole number between 1 and $total (got '$BEGIN_STEP')." >&2
        exit 1
    fi
    i=$((BEGIN_STEP - 1))
    echo "Starting at step $BEGIN_STEP/$total."
fi

while [ "$i" -lt "$total" ]; do
    echo
    echo "[Step $((i + 1))/$total] ${labels[$i]}"
    if step_defaults_to_no "${commands[$i]}"; then
        read -rp "Action? (y=run, Enter/S=skip, B=back, F=forward without running, Q=quit): " choice
        # Empty answer skips rather than runs.
        [ -z "$choice" ] && choice=S
    else
        read -rp "Action? (Y/Enter=run, S=skip, B=back, F=forward without running, Q=quit): " choice
    fi

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
