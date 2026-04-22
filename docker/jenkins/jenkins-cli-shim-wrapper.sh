#!/bin/sh

# Jenkins CLI Shim Wrapper
# This script serves as a wrapper for the Jenkins CLI, ensuring that the CLI JAR is extracted from the Jenkins WAR if it
# doesn't already exist. It also handles authentication using the initial admin password if available.
# Usage (Run the script with Jenkins CLI arguments):
#      jenkins-cli [CLI arguments]
# Examples:
#      jenkins-cli list-jobs
#      jenkins-cli build my-job
#      jenkins-cli -s http://jenkins:9499 -u admin -p mypassword list-jobs
#
# Note: Jenkins has CORS restrictions. I had to set the jenkins URL to http://jenkins:9499/jobs to avoid CORS issues on
# dev. You could allow CORS in jenkins using startup parameters, but that would be a security risk.
#


# Paths
JENKINS_DIR="/opt/jenkins"
WAR_PATH="$JENKINS_DIR/jenkins.war"
CLI_JAR="$JENKINS_DIR/jenkins-cli.jar"

# Configuration Defaults

# Set base URL if not provided via environment variable, default to http://jenkins:9499/jobs
if [ -z "$BASE_URL" ]; then
    BASE_URL="http://jenkins:$JENKINS_PORT/jobs"
fi
SECRET_PATH="$JENKINS_HOME/secrets/initialAdminPassword"

# 1. Self-extracting logic: extracts if the JAR is missing
if [ ! -f "$CLI_JAR" ]; then
    echo "Extracting Jenkins CLI jar..."
    # unzip -p streams the content of the first match to stdout
    unzip -p "$WAR_PATH" "WEB-INF/lib/cli-*.jar" > "$CLI_JAR"

    if [ $? -ne 0 ] || [ ! -s "$CLI_JAR" ]; then
        echo "Error: Could not extract CLI jar from $WAR_PATH"
        exit 1
    fi
fi

# 2. Authentication setup
AUTH_ARGS=""
if [ -f "$SECRET_PATH" ]; then
    AUTH_ARGS="-auth admin:$(cat "$SECRET_PATH")"
fi

# 3. Execution
# Passes current URL, auth, and all user arguments ("$@") to the real binary
exec java -jar "$CLI_JAR" -s "$BASE_URL" $AUTH_ARGS "$@"
