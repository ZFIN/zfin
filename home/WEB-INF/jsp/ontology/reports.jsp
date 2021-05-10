<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    Back to <a href="/action/devtool/home"> Dev-tools</a>

    <p></p>

    <span class="summaryTitle">Ontology Reports</span>

    <table class="searchresults">
        <tr>
            <td>
                <a href="/action/ontology/obsolete-term-report">Obsoleted Term Report</a>
            </td>
        </tr>
        <tr>
            <td>
                <a href="/action/ontology/secondary-term-report">Secondary Term Report</a>
            </td>
        </tr>
        <tr>
            <td>
                <a href="/action/ontology/term-usage-report">Ontology Term Usage Report</a>
            </td>
        </tr>
        <tr>
            <td><a href="/action/devtool/fx-stage-range-violations">FX Stage Range Violations</a></td>
        </tr>
        <tr>
            <td><a href="/action/devtool/merged-terms-used-in-relationships">Merged Terms used in Term Relationships</a></td>
        </tr>
        <tr>
            <td><a href="/action/devtool/terms-without-relationships">Active Terms without Term Relationships</a></td>
        </tr>
    </table>
</z:page>