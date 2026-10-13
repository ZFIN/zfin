import jenkins.model.Jenkins
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy

// Runs on every Jenkins startup (deployed by `ant deploy-jobs`).
//
// DISABLE_OIDC=true  -> swap the OIDC (bouncer) security realm for Jenkins' own
//                       user database, so Jenkins works without keycloak. The OIDC
//                       realm and authorization matrix are saved first so they can
//                       be put back. If no local account exists yet, an 'admin'
//                       account is created (password from JENKINS_ADMIN_PASSWORD,
//                       or generated and written to secrets/initialAdminPassword,
//                       where jenkins-cli also picks it up).
// DISABLE_OIDC unset/false -> restore the saved OIDC configuration. If Jenkins is on
//                       its own user database and nothing was saved, shut Jenkins
//                       down with instructions for redeploying config.xml.

def jenkins = Jenkins.get()
def backup = new File(jenkins.rootDir, "zfin-oidc-security.xml")
def disableOidc = System.getenv("DISABLE_OIDC") == "true"
def log = { msg -> println "[zfin-security-realm] ${msg}" }

if (disableOidc) {
    if (!(jenkins.securityRealm instanceof HudsonPrivateSecurityRealm)) {
        backup.text = Jenkins.XSTREAM2.toXML([
                securityRealm        : jenkins.securityRealm,
                authorizationStrategy: jenkins.authorizationStrategy,
        ])
        // The OIDC matrix grants rights to keycloak groups, which local accounts
        // don't have, so any logged-in local user gets full control instead.
        def authz = new FullControlOnceLoggedInAuthorizationStrategy()
        authz.allowAnonymousRead = false
        jenkins.securityRealm = new HudsonPrivateSecurityRealm(false)
        jenkins.authorizationStrategy = authz
        jenkins.save()
        log "DISABLE_OIDC=true: switched to the Jenkins user database (OIDC config saved to ${backup})"
    }

    def realm = (HudsonPrivateSecurityRealm) jenkins.securityRealm
    if (realm.allUsers.isEmpty()) {
        def password = System.getenv("JENKINS_ADMIN_PASSWORD")
        if (!password) {
            password = UUID.randomUUID().toString().replace('-', '')
            def secret = new File(jenkins.rootDir, "secrets/initialAdminPassword")
            secret.parentFile.mkdirs()
            secret.text = password + "\n"
            secret.setReadable(false, false)
            secret.setReadable(true, true)
            log "created local account 'admin'; password is in ${secret}"
        } else {
            log "created local account 'admin' with the password from JENKINS_ADMIN_PASSWORD"
        }
        realm.createAccount("admin", password)
    }
} else if (jenkins.securityRealm instanceof HudsonPrivateSecurityRealm) {
    if (backup.exists()) {
        def saved = (Map) Jenkins.XSTREAM2.fromXML(backup.text)
        jenkins.securityRealm = saved.securityRealm
        jenkins.authorizationStrategy = saved.authorizationStrategy
        jenkins.save()
        backup.delete()
        log "DISABLE_OIDC is off: restored the saved OIDC security realm"
    } else {
        // Refuse to keep running on local accounts when OIDC was asked for: stop
        // Jenkins so the misconfiguration can't go unnoticed.
        def config = new File(jenkins.rootDir, "config.xml")
        def msg = """
================================================================================
[zfin-security-realm] ERROR: DISABLE_OIDC is not 'true', but Jenkins is using its
own user database and there is no saved OIDC configuration to restore
(${backup} is missing). Jenkins will now shut down.

To restore OIDC (bouncer) login from server_apps/jenkins/config.xml, run from docker/:
    docker compose stop jenkins
    docker compose run --rm compile bash -lc "mv ${config} ${config}.local-users"
    docker compose run --rm compile bash -lc "ant deploy-jobs"
    docker compose up -d jenkins
(deploy-jobs only installs config.xml when none exists, hence moving it aside.
Any settings changed through the Jenkins UI must be re-applied afterwards.)

To keep using the Jenkins user database instead, set DISABLE_OIDC=true.
================================================================================
"""
        System.err.println(msg)
        println msg
        System.exit(1)
    }
}
