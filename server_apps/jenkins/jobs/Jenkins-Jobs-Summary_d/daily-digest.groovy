import jenkins.model.Jenkins
import hudson.model.Result
import java.text.SimpleDateFormat

// Get current time and time frame
def thisBuild = Thread.currentThread().executable
// If above doesn't work, try this method: https://www.reddit.com/r/jenkinsci/comments/9suejs/using_javalangthread_executable/


def previousBuild = thisBuild.getPreviousBuild()
def now = System.currentTimeMillis()
def timeFrameStart

if (previousBuild != null) {
    timeFrameStart = previousBuild.getTimeInMillis()
} else {
    timeFrameStart = now - (24 * 60 * 60 * 1000) // Default to 24 hours if no previous run
}

// Enforce minimum 24 hour window
def minWindowStart = now - (24 * 60 * 60 * 1000)
if (timeFrameStart > minWindowStart) {
    timeFrameStart = minWindowStart
}

// Cap at 72 hours
def maxLookback = 72L * 60 * 60 * 1000
if ((now - timeFrameStart) > maxLookback) {
    timeFrameStart = now - maxLookback
}

def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

// Collect all builds from time frame
def recentBuilds = []
// Keep track of jobs we've already recorded (only include the most recent build per job)
def seenJobs = [] as Set

for (job in Jenkins.get().allItems(hudson.model.Job)) {
    for (build in job.getBuilds()) {
        if (build.getTimeInMillis() >= timeFrameStart) {
            if (seenJobs.contains(job.fullName)) {
                break // we've already added the most recent build for this job
            }
            def status = "SUCCESS"
            if (build.result == Result.FAILURE) {
                status = "FAILED"
            } else if (build.result == Result.UNSTABLE) {
                status = "UNSTABLE"
            }

            recentBuilds.add([
                jobName: job.fullName,
                buildNumber: build.number,
                status: status,
                result: build.result,
                url: build.absoluteUrl
            ])
            seenJobs.add(job.fullName)
        } else {
            break // Builds are in reverse chronological order
        }
    }
}

// Count failures and unstable per job
// Categorize builds and calculate streaks
def newFailingBuilds = []
def failedBuilds = []
def unstableBuilds = []
def successfulBuilds = []
def buildStreaks = [:]
def buildHistories = [:]
def historyDepth = 4

// Walk back from a Run and return its last n results, newest first.
def collectHistory = { startRun, int n ->
    def results = []
    def cursor = startRun
    while (cursor != null && results.size() < n) {
        results.add(cursor.result)
        cursor = cursor.getPreviousBuild()
    }
    results
}

// Render a list of Result values (newest first) as colored dots using inline styles.
def renderSparkline = { List results ->
    if (!results) return ""
    def sb = new StringBuilder("<span style=\"margin-left:8px;white-space:nowrap;\">")
    results.each { r ->
        def color = "#9e9e9e" // null / aborted
        if (r == Result.SUCCESS)      color = "#4caf50"
        else if (r == Result.UNSTABLE) color = "#ff9800"
        else if (r == Result.FAILURE)  color = "#d32f2f"
        sb.append("<span style=\"display:inline-block;width:9px;height:9px;border-radius:50%;background-color:${color};margin-right:3px;\"></span>")
    }
    sb.append("</span>")
    sb.toString()
}

recentBuilds.each { buildInfo ->
    // Resolve Run object
    def job = Jenkins.get().getItemByFullName(buildInfo.jobName)
    def run = job.getBuildByNumber(buildInfo.buildNumber)

    int streak = 0
    if (run != null) {
        def tempRun = run
        // Count consecutive builds with SAME result, cap at 10
        while (tempRun != null && tempRun.result == buildInfo.result && streak < 10) {
            streak++
            tempRun = tempRun.getPreviousBuild()
        }
    }

    // Store streak and history using unique key
    def key = "${buildInfo.jobName}#${buildInfo.buildNumber}"
    buildStreaks[key] = streak
    buildHistories[key] = collectHistory(run, historyDepth)

    if (buildInfo.status == "FAILED") {
        if (streak.equals(1)) {
            newFailingBuilds.add(buildInfo)
        } else {
            failedBuilds.add(buildInfo)
        }
    } else if (buildInfo.status == "UNSTABLE") {
        unstableBuilds.add(buildInfo)
    } else {
        successfulBuilds.add(buildInfo)
    }
}

// Collect jobs whose last completed build failed but fell outside the time window
def jobsAlreadyReportedAsFailing = (newFailingBuilds + failedBuilds).collect { it.jobName } as Set
def outsideWindowFailures = []

// Collect jobs whose last completed build was UNSTABLE but fell outside the time window
// (similar logic to outsideWindowFailures)
def jobsAlreadyReportedAsUnstable = (newFailingBuilds + failedBuilds + unstableBuilds).collect { it.jobName } as Set
def outsideWindowUnstables = []

for (job in Jenkins.get().allItems(hudson.model.Job)) {
    if (!job.isBuildable()) continue
    if (jobsAlreadyReportedAsFailing.contains(job.fullName)) continue

    def lastBuild = job.getLastCompletedBuild()
    if (lastBuild == null) continue
    if (lastBuild.result != Result.FAILURE) continue
    if (lastBuild.getTimeInMillis() >= timeFrameStart) continue // already covered by in-window logic

    outsideWindowFailures.add([
        jobName: job.fullName,
        buildNumber: lastBuild.number,
        status: "FAILED",
        result: lastBuild.result,
        url: lastBuild.absoluteUrl
    ])
    buildHistories["${job.fullName}#${lastBuild.number}"] = collectHistory(lastBuild, historyDepth)
}

for (job in Jenkins.get().allItems(hudson.model.Job)) {
    if (!job.isBuildable()) continue
    if (jobsAlreadyReportedAsUnstable.contains(job.fullName)) continue

    def lastBuild = job.getLastCompletedBuild()
    if (lastBuild == null) continue
    if (lastBuild.result != Result.UNSTABLE) continue
    if (lastBuild.getTimeInMillis() >= timeFrameStart) continue // already covered by in-window logic

    outsideWindowUnstables.add([
        jobName: job.fullName,
        buildNumber: lastBuild.number,
        status: "UNSTABLE",
        result: lastBuild.result,
        url: lastBuild.absoluteUrl
    ])
    buildHistories["${job.fullName}#${lastBuild.number}"] = collectHistory(lastBuild, historyDepth)
}

// Print results to console
println "=== Jobs that ran since ${dateFormat.format(new Date(timeFrameStart))} ==="
println ""

if (newFailingBuilds.size() > 0) {
    println "NEW FAILING JOBS:"
    newFailingBuilds.each { build ->
        println "  ${build.jobName} #${build.buildNumber} - ${build.status} (failed 1 times)"
    }
    println ""
}

if (failedBuilds.size() > 0) {
    println "FAILED JOBS:"
    failedBuilds.each { build ->
        println "  ${build.jobName} #${build.buildNumber} - ${build.status} (failed ${buildStreaks[build.jobName + '#' + build.buildNumber]} times in a row)"
    }
    println ""
}

if (unstableBuilds.size() > 0) {
    println "UNSTABLE JOBS:"
    unstableBuilds.each { build ->
        println "  ${build.jobName} #${build.buildNumber} - ${build.status} (unstable ${buildStreaks[build.jobName + '#' + build.buildNumber]} times in a row)"
    }
    println ""
}

if (successfulBuilds.size() > 0) {
    println "SUCCESSFUL JOBS:"
    successfulBuilds.each { build ->
        println "  ${build.jobName} #${build.buildNumber} - ${build.status} (succeeded ${buildStreaks[build.jobName + '#' + build.buildNumber]} times in a row)"
    }
    println ""
}

if (outsideWindowFailures.size() > 0) {
    println "FAILING JOBS (last run failed, outside window):"
    outsideWindowFailures.each { build ->
        println "  ${build.jobName} #${build.buildNumber} - ${build.status}"
    }
}

if (outsideWindowUnstables.size() > 0) {
    println "UNSTABLE JOBS (last run unstable, outside window):"
    outsideWindowUnstables.each { build ->
        println "  ${build.jobName} #${build.buildNumber} - ${build.status}"
    }
}

println "\nTotal builds in period: ${recentBuilds.size()}"

// Generate HTML report for email
// Total count of all failing jobs (in-window new/repeated + outside-window failures)
def allFailingCount = newFailingBuilds.size() + failedBuilds.size() + outsideWindowFailures.size()

def html = new StringBuilder()
html.append("""<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: Arial, sans-serif; margin: 20px; }
  h1 { color: #333; }
  h2 { color: #555; margin-top: 20px; }
  .summary { background: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
  .summary-item { display: inline-block; margin-right: 30px; }
  .count { font-size: 24px; font-weight: bold; }
  .new-failing { color: #d32f2f; }
  .failed { color: #f44336; }
  .unstable { color: #ff9800; }
  .success { color: #4caf50; }
  table { border-collapse: collapse; width: 100%; margin-top: 10px; }
  th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
  th { background-color: #f2f2f2; }
  tr:nth-child(even) { background-color: #f9f9f9; }
  a { color: #1976d2; text-decoration: none; }
  a:hover { text-decoration: underline; }
  .timestamp { color: #666; font-size: 12px; }
</style>
</head>
<body>
<h1>Jenkins Jobs Summary</h1>
<p class="timestamp">Period: ${dateFormat.format(new Date(timeFrameStart))} to ${dateFormat.format(new Date(now))}</p>
<p class="timestamp">Report generated: ${dateFormat.format(new Date())}</p>
<p class="timestamp">
    <strong>${recentBuilds.size()} jobs run in reporting window</strong>
</p>

<div class="summary">
  <div class="summary-item">
    <span class="count success">${successfulBuilds.size()}</span> Successful
  </div>
  <div class="summary-item">
    <span class="count new-failing">${newFailingBuilds.size()}</span> New Failures
  </div>
  <div class="summary-item">
    <span class="count failed">${failedBuilds.size()}</span> Repeated Failures
  </div>
  <div class="summary-item">
    <span class="count unstable">${unstableBuilds.size()}</span> Unstable
  </div>
  <div class="summary-item">
    <span class="count failed">${allFailingCount}</span> All Failing Jobs
  </div>
</div>
""")

if (newFailingBuilds.size() > 0) {
    html.append("""
<h2 class="new-failing">New Failing Jobs (Requires Attention)</h2>
<table>
  <tr><th>Job Name</th><th>Build #</th><th>Status</th></tr>
""")
    newFailingBuilds.each { build ->
        html.append("  <tr><td><a href=\"${build.url}\">${build.jobName}</a>${renderSparkline(buildHistories[build.jobName + '#' + build.buildNumber])}</td><td>${build.buildNumber}</td><td class=\"new-failing\">FAILED (new)</td></tr>\n")
    }
    html.append("</table>\n")
}

if (failedBuilds.size() > 0) {
    html.append("""
<h2 class="failed">Repeatedly Failing Jobs</h2>
<table>
  <tr><th>Job Name</th><th>Build #</th><th>Failure Streak</th></tr>
""")
    failedBuilds.each { build ->
        html.append("  <tr><td><a href=\"${build.url}\">${build.jobName}</a>${renderSparkline(buildHistories[build.jobName + '#' + build.buildNumber])}</td><td>${build.buildNumber}</td><td class=\"failed\">${buildStreaks[build.jobName + '#' + build.buildNumber]} times</td></tr>\n")
    }
    html.append("</table>\n")
}

if (unstableBuilds.size() > 0) {
    html.append("""
<h2 class="unstable">Unstable Jobs</h2>
<table>
  <tr><th>Job Name</th><th>Build #</th><th>Unstable Streak</th></tr>
""")
    unstableBuilds.each { build ->
        html.append("  <tr><td><a href=\"${build.url}\">${build.jobName}</a>${renderSparkline(buildHistories[build.jobName + '#' + build.buildNumber])}</td><td>${build.buildNumber}</td><td class=\"unstable\">${buildStreaks[build.jobName + '#' + build.buildNumber]} times</td></tr>\n")
    }
    html.append("</table>\n")
}

if (successfulBuilds.size() > 0) {
    html.append("""
<h2 class="success">Successful Jobs</h2>
<table>
  <tr><th>Job Name</th><th>Build #</th><th>Success Streak</th></tr>
""")
    successfulBuilds.each { build ->
        html.append("  <tr><td><a href=\"${build.url}\">${build.jobName}</a>${renderSparkline(buildHistories[build.jobName + '#' + build.buildNumber])}</td><td>${build.buildNumber}</td><td class=\"success\">${buildStreaks[build.jobName + '#' + build.buildNumber]} times</td></tr>\n")
    }
    html.append("</table>\n")
}

if (outsideWindowFailures.size() > 0) {
    html.append("""
<h2 class="new-failing">Failing Jobs (last run failed, ${outsideWindowFailures.size()})</h2>
<p class="timestamp">Jobs whose most recent build failed prior to this reporting window.</p>
<table>
  <tr><th>Job Name</th><th>Build #</th><th>Status</th></tr>
""")
    outsideWindowFailures.each { build ->
        html.append("  <tr><td><a href=\"${build.url}\">${build.jobName}</a>${renderSparkline(buildHistories[build.jobName + '#' + build.buildNumber])}</td><td>${build.buildNumber}</td><td class=\"new-failing\">FAILED</td></tr>\n")
    }
    html.append("</table>\n")
}

if (outsideWindowUnstables.size() > 0) {
    html.append("""
<h2 class="unstable">Unstable Jobs (last run unstable, ${outsideWindowUnstables.size()})</h2>
<p class="timestamp">Jobs whose most recent build was unstable prior to this reporting window.</p>
<table>
  <tr><th>Job Name</th><th>Build #</th><th>Status</th></tr>
""")
    outsideWindowUnstables.each { build ->
        html.append("  <tr><td><a href=\"${build.url}\">${build.jobName}</a>${renderSparkline(buildHistories[build.jobName + '#' + build.buildNumber])}</td><td>${build.buildNumber}</td><td class=\"unstable\">UNSTABLE</td></tr>\n")
    }
    html.append("</table>\n")
}

html.append("""
</body>
</html>
""")

// Write HTML report to workspace
def build = Thread.currentThread().executable
def workspace = build.workspace
def reportFile = new File(workspace.getRemote(), "jenkins-jobs-summary.html")
reportFile.text = html.toString()
println "\nHTML report written to: ${reportFile.absolutePath}"
