<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<style>
    #inner {
        margin-left: 30px;
        margin-right: 50px;
    }
</style>
<div id="inner">

    <div class="page-header">
        <h1>ZFIN Application: Developer Tools
        </h1>
    </div>
    <div class="page-header">
        <div id="readonly-mode" style="display: none">
            <h3>System Status:
                <span class="alert alert-danger">Read-only mode</span>
                <span>
                        <button onclick="changeSystemMode(false)" type="button"
                                class="btn btn-link">Make Normal</button>
                    </span>
            </h3>
        </div>
        <div id="normal-mode">
            <h3>System Status:
                <span class="alert alert-info">Normal mode</span>
                <span>
                        <button onclick="changeSystemMode(true)" type="button"
                                class="btn btn-link">Make Read-Only</button>
                    </span>
            </h3>
        </div>
        <div id="error-message" style="display: none">
            <span class="error">Could not save update </span>
        </div>
    </div>

    <div class="row">
        <div class="table-responsive col-md-6">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th class="col-md-1">Testing Tools</th>
                    <th class="col-md-2">Job Dashboard</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/ontology/summary">Check Ontology Chache</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/blast-jobs">Blast Jobs</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/gwt/modules">GWT Modules</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/blast/blast-definitions">Blast Contents</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                    </td>
                    <td class="col-md-2">
                        <a href="/action/blast/blast-files">Downloadable Blast Files</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/ontology/version-info">Ontology Version Info</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/display-groups">ReferenceDB/DBLink Display Groups</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/test-error-page">Test Error Page</a>
                        &nbsp;
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/blastable-databases">Blast Database Groups</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/request/ZDB-LAB-000114-8">Test jQuery Ajax Lab Page</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/phenotype-curation-history">History: Phenotype Experiments</a>
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td class="col-md-2">
                        <a href="/action/devtool/phenotype-curation-history-statements">History: Phenotype
                            Statements</a>
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td class="col-md-2">
                        <a href="/action/ontology/reports">Ontology Reports</a>
                    </td>
                </tr>
                <tr>
                    <th class="col-md-1">JVM Configuration</th>
                    <th class="col-md-2">Session Info</th>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/java-properties">Java Properties</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/test-browser">Check HTTP header of your browser</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/thread-info">Thread Info</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/view-session-info">Current Session Info</a>
                        -
                        <a href="/action/devtool/view-global-session">Global Session Info</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/classpath-info">View Classpath Info</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/servlet-context">Servlet Context Info</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        &nbsp;
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/application-context">Application Context Info</a>
                    </td>
                </tr>
                <tr>
                    <th class="col-md-1">ZFIN Properties</th>
                    <th class="col-md-2">Miscellaneous</th>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/zfin-properties">Zfin Properties</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/zfin-statistics">ZFIN Statistics</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                    </td>
                    <td class="col-md-2">
                        <a href="/action/database/browse-database">Browse Database tables</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                    </td>
                    <td class="col-md-2">
                        <a href="/action/unload/summary">Browse Data History</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                    </td>
                    <td class="col-md-2">
                        <a href="/action/unload/index-summary">Site Search Index</a>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                    </td>
                    <td class="col-md-2">
                        <a href="/action/unload/downloads/archive">Download Files Archive</a>
                    </td>
                </tr>
                <tr>
                    <th class="col-md-1">
                        <b>Database Configuration</b>
                    </td>
                    <td>
                        <b>&nbsp;</b>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/database-info">Database Info</a><br/>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/devtool/bts-index-info">BTS Index Info</a><br/>
                    </td>
                </tr>
                <tr>
                    <td class="col-md-1">
                        <a href="/action/devtool/jdbc-driver-info">JDBC Driver Info</a>
                    </td>
                    <td class="col-md-2">
                        <a href="/action/database/overview">Informix Database </a>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
    jQuery.ajax({
        url: "/action/devtool/system-status",
        success: function (data) {

            if (data.readonly) {
                jQuery("#readonly-mode").show();
                jQuery("#normal-mode").hide();
            } else {
                jQuery("#readonly-mode").hide();
                jQuery("#normal-mode").show();
            }

        }
    });

    function changeSystemMode(readonly) {
        jQuery.ajax({
            method: "POST",
            url: "/action/devtool/system-status",
            data: {readonlyMode: readonly},
            success: function (data) {
                jQuery("#readonly-mode").toggle();
                jQuery("#normal-mode").toggle();
                jQuery("#error-message").hide();
            },
            error: function (data) {
                jQuery("#error-message").show();
            }
        });
    }
</script>
