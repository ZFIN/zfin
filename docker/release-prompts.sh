#!/bin/bash

# Interactive ZFIN release driver. Walks through deployment steps with
# forward/back navigation so you can re-run a step without restarting.
# Auto-wraps in a screen session and a `script` typescript recording.
# Set NO_SCREEN=1 or NO_SCRIPT=1 to opt out of either wrapper
# (e.g. on macOS where `script` flags differ).

cmprun() {
    docker compose run --rm compile bash -lc "$1"
}

# `gradle loaddb` drops and reloads the database, so it must never run
# against production. Check both the hostname and the bound address: the
# box answers as zfin.org on this IP whatever it calls itself, and a
# rename shouldn't silently disarm the guard.
PROD_HOSTNAME=franklin.zfin.org
PROD_IP=184.171.92.30

this_hostname() {
    hostname -f 2>/dev/null || hostname 2>/dev/null || echo unknown
}

is_production_host() {
    case "$(this_hostname)" in
        "$PROD_HOSTNAME"|franklin) return 0 ;;
    esac

    # hostname -I is GNU-only; ip and ifconfig cover the rest. -F and -w/-x
    # so the dots aren't read as regex wildcards and so a longer address that
    # merely starts with these digits can't match.
    hostname -I 2>/dev/null | tr ' ' '\n' | grep -qxF "$PROD_IP" && return 0
    ip -o addr show 2>/dev/null | grep -qwF "$PROD_IP" && return 0
    ifconfig -a 2>/dev/null | grep -qwF "$PROD_IP" && return 0

    return 1
}

loaddb() {
    if is_production_host; then
        echo
        echo "  !! REFUSING to run 'gradle loaddb' on $(this_hostname)." >&2
        echo "  !! It drops and reloads the database, and this host is production" >&2
        echo "  !! ($PROD_HOSTNAME / $PROD_IP). Nothing was run." >&2
        echo
        return 1
    fi
    cmprun 'gradle loaddb'
}

# Steps that default to NO: Enter skips them and you must type Y to run.
# Matched on the commands[] entry, so reordering steps can't misalign this.
step_defaults_to_no() {
    case "$1" in
        loaddb|"cmprun 'ant create-views'") return 0 ;;
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
