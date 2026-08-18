#!/bin/sh

# Jenkins CLI Shim Wrapper
#
# Wraps the Jenkins CLI: extracts the CLI JAR from the Jenkins WAR on first use,
# resolves credentials, verifies them before running a command, and -- when auth
# is missing or wrong -- prints a short message plus setup instructions instead
# of dumping a raw 401 / "anonymous is missing the Overall/Read permission" error.
#
# Usage:
#      jenkins-cli <command> [args...]       run a Jenkins CLI command
#      jenkins-cli configure-admin-token [--create-admin]
#                                            set up an admin API token (startup script; needs restart);
#                                            --create-admin also creates the account if missing
#      jenkins-cli login <user> <token>      store API-token credentials (persisted)
#      jenkins-cli logout                    remove stored credentials
#      jenkins-cli help                       list Jenkins CLI commands (passes through)
#      jenkins-cli --help | -h               short usage
#      jenkins-cli --help-verbose            this detailed help + auth setup
#
# Examples:
#      jenkins-cli list-jobs
#      jenkins-cli build my-job
#
# Bootstrapping auth when no admin account exists yet (run on the Docker host,
# outside the container) -- creates the admin user and configures the CLI token:
#      zexec jenkins -c 'jenkins-cli configure-admin-token --create-admin'
#      zrestart jenkins
#
# Credential resolution order (first match wins):
#   1. JENKINS_CLI_USER + JENKINS_CLI_PASSWORD environment variables
#   2. stored credentials file (see `jenkins-cli login`), persisted on the
#      jenkins-home volume so it survives container restarts
#   3. the initial admin password file, if Jenkins still has one
#
# Note: Jenkins has CORS restrictions, so BASE_URL points at the /jobs prefix
# (http://jenkins:9499/jobs). Allowing CORS via startup params would be a
# security risk, so we don't.

# Paths
JENKINS_DIR="${JENKINS_DIR:-/opt/jenkins}"
WAR_PATH="$JENKINS_DIR/jenkins.war"
CLI_JAR="$JENKINS_DIR/jenkins-cli.jar"

# JENKINS_HOME may be unset in the environment when invoked via `docker exec`;
# fall back to the volume-mounted location so credential storage still works.
JENKINS_HOME="${JENKINS_HOME:-/opt/zfin/www_homes/zfin.org/server_apps/jenkins/jenkins-home}"
SECRET_PATH="$JENKINS_HOME/secrets/initialAdminPassword"

# Persistent CLI credentials. Lives on the jenkins-home volume so it survives
# container rebuilds/restarts. Override with JENKINS_CLI_AUTH_FILE.
CREDS_FILE="${JENKINS_CLI_AUTH_FILE:-$JENKINS_HOME/secrets/jenkins-cli-auth}"

# Base URL (internal). Default to http://jenkins:<port>/jobs.
JENKINS_PORT="${JENKINS_PORT:-9499}"
if [ -z "$BASE_URL" ]; then
    BASE_URL="http://jenkins:$JENKINS_PORT/jobs"
fi

# --- helpers ---------------------------------------------------------------

# Best-effort public site URL, read from Jenkins' own location config. Used only
# to build the "create an API token" link in the help text.
site_url() {
    loc_cfg="$JENKINS_HOME/jenkins.model.JenkinsLocationConfiguration.xml"
    if [ -f "$loc_cfg" ]; then
        sed -n 's:.*<jenkinsUrl>\(.*\)</jenkinsUrl>.*:\1:p' "$loc_cfg" | head -1
    fi
}

# Build the API-token security page URL for a given username.
security_url() {
    base="$(site_url)"
    [ -z "$base" ] && base="https://<SITE>.zfin.org/jobs"
    base="${base%/}"
    printf '%s/user/%s/security/' "$base" "$1"
}

# The username we can best guess for the help link.
username_hint() {
    if [ -n "$JENKINS_CLI_USER" ]; then
        printf '%s' "$JENKINS_CLI_USER"
    elif [ -f "$CREDS_FILE" ]; then
        cut -d: -f1 "$CREDS_FILE"
    else
        printf '<your-username>'
    fi
}

print_auth_help() {
    user="$(username_hint)"
    cat >&2 <<EOF

The Jenkins CLI needs an API token to authenticate. To set one up:

  1. Create an API token on your Jenkins user security page:
       $(security_url "$user")
     Click "Add new Token", name it, and copy the generated token.
     (If the host above shows <SITE>, replace it with this server's hostname.)

  2. Store it so the CLI remembers it (persisted on the jenkins-home volume):
       jenkins-cli login <your-username> <api-token>

For a one-off command without storing anything, set env vars instead:
       JENKINS_CLI_USER=<user> JENKINS_CLI_PASSWORD=<token> jenkins-cli <command>

If you administer this Jenkins, set up an admin token via a startup script
(no credentials needed, but requires a Jenkins restart):
       jenkins-cli configure-admin-token

Run 'jenkins-cli help' for the list of Jenkins commands once authenticated.
EOF
}

short_usage() {
    cat <<EOF
jenkins-cli -- wrapper around the Jenkins CLI

Usage: jenkins-cli <command> [args...]

  jenkins-cli list-jobs                 example command
  jenkins-cli help                      list all Jenkins CLI commands

Authentication:
  jenkins-cli configure-admin-token     set up an admin API token (needs a restart)
       [--create-admin]                 ...also create the account if it is missing
  jenkins-cli login <user> <token>      store API-token credentials
  jenkins-cli logout                    remove stored credentials

  jenkins-cli --help-verbose            detailed help + credential setup
EOF
}

extract_jar() {
    if [ ! -f "$CLI_JAR" ]; then
        echo "Extracting Jenkins CLI jar..." >&2
        # unzip -p streams the content of the first match to stdout
        unzip -p "$WAR_PATH" "WEB-INF/lib/cli-*.jar" > "$CLI_JAR"
        if [ $? -ne 0 ] || [ ! -s "$CLI_JAR" ]; then
            echo "Error: Could not extract CLI jar from $WAR_PATH" >&2
            exit 1
        fi
    fi
}

# Resolve AUTH_ARGS from env, stored creds file, or initial admin password.
resolve_auth() {
    AUTH_ARGS=""
    if [ -n "$JENKINS_CLI_USER" ] && [ -n "$JENKINS_CLI_PASSWORD" ]; then
        AUTH_ARGS="-auth $JENKINS_CLI_USER:$JENKINS_CLI_PASSWORD"
    elif [ -f "$CREDS_FILE" ]; then
        AUTH_ARGS="-auth @$CREDS_FILE"
    elif [ -f "$SECRET_PATH" ]; then
        AUTH_ARGS="-auth admin:$(cat "$SECRET_PATH")"
    fi
}

# Run the real CLI. Relies on word-splitting of $AUTH_ARGS (token values have no
# spaces), matching the original shim's behavior.
run_cli() {
    java -jar "$CLI_JAR" -s "$BASE_URL" -http $AUTH_ARGS "$@"
}

# Verify the resolved credentials with a cheap `who-am-i` call.
# Returns: 0 ok, 2 auth problem (unauthenticated / anonymous), 3 other error.
check_auth() {
    out="$(run_cli who-am-i 2>&1)"
    rc=$?
    if [ $rc -ne 0 ]; then
        case "$out" in
            *"missing the Overall/Read"*|*401*|*"handshake failed"*|*[Aa]uthenticat*|*anonymous*)
                return 2 ;;
            *)
                AUTH_ERR="$out"
                return 3 ;;
        esac
    fi
    case "$out" in
        *"Authenticated as: anonymous"*) return 2 ;;
    esac
    return 0
}

print_admin_token_instructions() {
    user="$(username_hint)"
    cat >&2 <<EOF

To set up an admin API token manually:
  1. Log in to Jenkins as an administrator in your browser.
  2. Create an API token at:
       $(security_url "$user")
     (Add new Token -> name it -> copy the generated token.)
  3. Store it for the CLI:
       jenkins-cli login <username> <api-token>
EOF
}

# Set up an admin API token without needing any existing credentials, by
# dropping a one-shot init.groovy.d script. Init scripts run as SYSTEM at
# startup, so they need no auth; the script mints the token, stores it for the
# CLI, then deletes itself so nothing lingers. Init scripts only run at startup
# (a config reload does not re-run them), so this requires a Jenkins restart --
# which must be triggered from the host, so we only write the script here.
configure_admin_token() {
    create="false"
    for arg in "$@"; do
        case "$arg" in
            --create-admin|--create) create="true" ;;
            *) echo "jenkins-cli: unknown option for configure-admin-token: '$arg'" >&2; return 2 ;;
        esac
    done
    admin_user="${JENKINS_CLI_ADMIN_USER:-admin}"
    init_dir="$JENKINS_HOME/init.groovy.d"
    script_path="$init_dir/zfin-cli-token.groovy"

    if ! mkdir -p "$init_dir" 2>/dev/null || [ ! -w "$init_dir" ]; then
        echo "jenkins-cli: cannot write to $init_dir" >&2
        echo "(run this inside the Jenkins container, where JENKINS_HOME is writable)" >&2
        print_admin_token_instructions
        return 1
    fi

    # Fill placeholders via sed so Groovy's own ${...} GStrings (kept literal by
    # the quoted heredoc) don't collide with shell expansion.
    sed -e "s|@@ADMIN@@|$admin_user|g" \
        -e "s|@@CREDS@@|$CREDS_FILE|g" \
        -e "s|@@SELF@@|$script_path|g" \
        -e "s|@@CREATE@@|$create|g" > "$script_path" <<'GROOVY'
import jenkins.model.Jenkins
import hudson.model.User
import jenkins.security.ApiTokenProperty
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy

// One-shot bootstrap written by `jenkins-cli configure-admin-token`.
// Mints an API token for the CLI admin user, stores it for the shim, then
// deletes itself so it never runs again. With --create-admin it will also
// create the account (and grant admin) if it does not exist.
def adminId = "@@ADMIN@@"
def credsFile = new File("@@CREDS@@")
def tokenName = "jenkins-cli"
def create = "@@CREATE@@" == "true"
def reserved = ["SYSTEM", "unknown"]
def jenkins = Jenkins.get()

// Real login accounts (exclude Jenkins' internal pseudo-users).
def realUsers = { User.getAll().collect { it.id }.findAll { !reserved.contains(it) } }

// Best-effort: ensure the user has admin rights under the active authz strategy.
def grantAdmin = { id ->
    def authz = jenkins.getAuthorizationStrategy()
    try {
        if (authz instanceof GlobalMatrixAuthorizationStrategy) {
            authz.add(Jenkins.ADMINISTER, id)
            jenkins.save()
            println "[cli-token] granted Overall/Administer to '${id}'"
        } else if (authz instanceof FullControlOnceLoggedInAuthorizationStrategy) {
            println "[cli-token] authz grants all logged-in users full control; no grant needed"
        } else {
            println "[cli-token] authz is ${authz.class.simpleName}; grant '${id}' admin manually if needed"
        }
    } catch (Exception e) {
        println "[cli-token] could not auto-grant admin (${e.message}); grant '${id}' manually"
    }
}

try {
    // getAll() forces a load of on-disk user records; getById alone can miss a
    // user that has not been loaded yet this early in startup.
    def user = reserved.contains(adminId) ? null :
        (User.getAll().find { it.id == adminId } ?: User.getById(adminId, false))

    if (user == null && create && !reserved.contains(adminId)) {
        def realm = jenkins.getSecurityRealm()
        if (realm instanceof HudsonPrivateSecurityRealm) {
            def pw = UUID.randomUUID().toString().replace('-', '')
            user = realm.createAccount(adminId, pw)
            println "[cli-token] created local account '${adminId}' (UI login password: ${pw})"
        } else {
            // External realm (e.g. OIDC): the token still works for the CLI, but
            // interactive login is handled by the realm, not this local record.
            user = User.getById(adminId, true)
            println "[cli-token] created user record '${adminId}' for token auth; interactive login is handled by ${realm.class.simpleName}"
        }
        grantAdmin(adminId)
    }

    if (user == null) {
        if (reserved.contains(adminId)) {
            println "[cli-token] '${adminId}' is a reserved internal account, not a usable login"
        } else {
            println "[cli-token] user '${adminId}' not found (re-run with --create-admin to create it)"
        }
        println "[cli-token] known users: ${realUsers()}"
        println "[cli-token] re-run: JENKINS_CLI_ADMIN_USER=<id> jenkins-cli configure-admin-token [--create-admin], then restart"
    } else {
        def store = user.getProperty(ApiTokenProperty.class).getTokenStore()
        store.getTokenListSortedByName().findAll { it.name == tokenName }.each { store.revokeToken(it.uuid) }
        def r = store.generateNewToken(tokenName)
        user.save()
        credsFile.parentFile.mkdirs()
        credsFile.setText("${user.id}:${r.plainValue}\n", "UTF-8")
        credsFile.setReadable(false, false)
        credsFile.setWritable(false, false)
        credsFile.setExecutable(false, false)
        credsFile.setReadable(true, true)
        credsFile.setWritable(true, true)
        println "[cli-token] wrote CLI credentials for '${user.id}'"
    }
} finally {
    new File("@@SELF@@").delete()
}
GROOVY

    echo "Wrote one-shot bootstrap script:"
    echo "    $script_path"
    echo
    if [ "$create" = "true" ]; then
        echo "It will create '$admin_user' if missing, grant it admin, mint an API token,"
        echo "store it for the CLI, and remove itself the next time Jenkins starts."
    else
        echo "It will mint an API token for '$admin_user', store it for the CLI, and"
        echo "remove itself the next time Jenkins starts."
    fi
    echo "Restart Jenkins to apply"
    echo "(from the Docker host, not inside the container):"
    echo "    zrestart jenkins        # or: docker compose restart jenkins"
    echo
    echo "After it restarts, verify with: jenkins-cli who-am-i"
    return 0
}

# --- subcommands -----------------------------------------------------------

case "${1:-}" in
    login)
        user="$2"
        token="$3"
        if [ -z "$user" ] || [ -z "$token" ]; then
            echo "Usage: jenkins-cli login <username> <api-token>" >&2
            echo "Create a token at: $(security_url '<your-username>')" >&2
            exit 2
        fi
        extract_jar
        # Verify before storing.
        AUTH_ARGS="-auth $user:$token"
        if ! out="$(run_cli who-am-i 2>&1)" || \
           printf '%s' "$out" | grep -q "Authenticated as: anonymous"; then
            echo "jenkins-cli: those credentials did not authenticate; not storing them." >&2
            print_auth_help
            exit 1
        fi
        mkdir -p "$(dirname "$CREDS_FILE")"
        ( umask 077; printf '%s:%s\n' "$user" "$token" > "$CREDS_FILE" )
        chmod 600 "$CREDS_FILE" 2>/dev/null
        echo "Stored credentials for '$user' in $CREDS_FILE"
        exit 0
        ;;
    logout)
        if [ -f "$CREDS_FILE" ]; then
            rm -f "$CREDS_FILE"
            echo "Removed stored credentials ($CREDS_FILE)."
        else
            echo "No stored credentials to remove."
        fi
        exit 0
        ;;
    configure-admin-token)
        shift
        configure_admin_token "$@"
        exit $?
        ;;
    ""|--help|-h)
        # Short usage. `jenkins-cli help` (no dashes) falls through to the real
        # CLI so its command list is shown instead.
        short_usage
        exit 0
        ;;
    --help-verbose)
        # Detailed help + auth setup. Print the contiguous comment header (after
        # the shebang), stopping at the first blank/non-comment line.
        awk 'NR>2 { if ($0 ~ /^#/) { sub(/^# ?/, ""); print } else exit }' "$0"
        print_auth_help
        exit 0
        ;;
esac

# --- main ------------------------------------------------------------------

extract_jar
resolve_auth

if [ -z "$AUTH_ARGS" ]; then
    echo "jenkins-cli: not authenticated (no credentials configured)." >&2
    print_auth_help
    exit 1
fi

if [ "${JENKINS_CLI_SKIP_PREFLIGHT:-0}" != "1" ]; then
    check_auth
    rc=$?
    if [ "$rc" = "2" ]; then
        echo "jenkins-cli: authentication failed." >&2
        print_auth_help
        exit 1
    elif [ "$rc" = "3" ]; then
        echo "jenkins-cli: could not reach Jenkins at $BASE_URL" >&2
        [ -n "$AUTH_ERR" ] && echo "$AUTH_ERR" >&2
        exit 1
    fi
fi

exec java -jar "$CLI_JAR" -s "$BASE_URL" -http $AUTH_ARGS "$@"
