<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.framework.presentation.PaginationBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" required="true" %>


<form:form method="Get" commandName="formBean" id="Expression Report" action="anatomy-phenotype-report-do-search">
    <table width="100%">
        <tr>
            <td width="30%" colspan="2">
                Select all anatomy structures and GO BP term for which you would like to find phenotype annotations.
            </td>
            <td width="70%">
            </td>
        </tr>
        <tr valign="top" bgcolor="#eeeeee">
            <td align="top" colspan="2">
                <b title="One or more anatomy structures that an antibody labels">Anatomy Terms</b>
                <br/>
                <form:hidden path="anatomyTermIDs"/>
                <form:hidden path="anatomyTermNames"/>
                <script type="text/javascript">
                    var LookupProperties = {
                        divName: "anatomyTerm",
                        inputName: "searchTerm",
                        showError: true,
                        <c:if test='${formBean.anatomyTermNames != null}' >
                        previousTableValues: "${formBean.anatomyTermNames}",
                        </c:if>
                        hiddenNames: "anatomyTermNames",
                        hiddenIds: "anatomyTermIDs",
                        type: "<%= LookupComposite.GDAG_TERM_LOOKUP %>",
                        ontologyName: "<%= Ontology.ANATOMY %>",
                        width: 40,
                        wildcard: false,
                        useTermTable: true
                    }
                </script>
                <style type="text/css">
                    .accessoryLabel {
                        font-size: .75em;
                    }
                </style>

                <link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
                <script language="javascript"
                        src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>

                <div id="anatomyTerm"></div>
                <table>
                    <tr>
                        <td valign="top" width="60%">
                            <form:checkbox path="includeSubstructures"/>
                            <label for="includeSubstructures2" class="accessoryLabel">Include
                                substructures</label>
                        </td>
                    </tr>
                </table>
            </td>
            <td align="top" colspan="2">
                <b title="One or more anatomy structures that an antibody labels">GO BP Terms</b>
                <br/>
                <form:hidden path="goTermIDs"/>
                <form:hidden path="goTermNames"/>
                <script type="text/javascript">
                    var LookupProperties2 = {
                        divName: "goTerm",
                        inputName: "searchGoTerm",
                        showError: true,
                        <c:if test='${formBean.goTermNames != null}' >
                        previousTableValues: "${formBean.goTermNames}",
                        </c:if>
                        hiddenNames: "goTermNames",
                        hiddenIds: "goTermIDs",
                        type: "<%= LookupComposite.GDAG_TERM_LOOKUP %>",
                        ontologyName: "<%= Ontology.GO_BP %>",
                        width: 40,
                        wildcard: false,
                        useTermTable: true
                    }
                </script>
                <style type="text/css">
                    .accessoryLabel {
                        font-size: .75em;
                    }
                </style>

                <div id="goTerm"></div>
                <table>
                    <tr>
                        <td valign="top" width="60%">
                            <form:checkbox path="includeSubstructuresGo"/>
                            <label for="includeSubstructuresGo1" class="accessoryLabel">Include
                                substructures</label>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="3">
            </td>
        </tr>
    </table>

    <table width=100%>
        <tr>
            <td align="right" bgcolor="#cccccc">
                <input value="Search" onclick="submitForm(1)" type="button">
                <input value="Reset" type="button" onclick="call_reset()">
                <input type="hidden" name="ACTION"/>
            </td>
        </tr>
    </table>
</form:form>


<script type="text/javascript">

    function call_reset() {
        document.getElementById("includeSubstructures2").checked = false;
        // clear the ao term table
        clearTable();
    }
    function submitForm(page) {
        var pauseIcrement = 500;  // wait up to 4 seconds
        var currentTime = new Date();  // wait up to 4 seconds
        try {
            validateLookup();
        }
        catch(e) {
            // if not defined, then keep going
        }
        var form = document.getElementById("Expression Report");
        var pageField = document.getElementById("<%= PaginationBean.PAGE %>");
        if (pageField != null)
            pageField.value = page;
        form.action.value = "SEARCH";
        try {
            termStatus = getValidationStatus();
            currentTime = new Date();  // wait up to 4 seconds
            intervalId = setInterval('doValidateAndSubmit(' + currentTime.getMilliseconds() + ');', pauseIcrement);
        }
        catch(e) {
            // if not defined or some other problem, then go ahead and submit
            form.submit();
        }
    }

    function doValidateAndSubmit(currentTime) {
        var maxTime = 5000;
        var diff = (new Date()).getMilliseconds() - currentTime;
        var inputElement = document.getElementById(LookupProperties.inputName);
        termStatus = getValidationStatus();

        // either there was an asynchronous connection failure, or we are still waiting,
        // in which case we should resubmit the query
        if (termStatus == "LOOKING" || termStatus == "FAILURE" && diff < maxTime) {
            validateLookup();
            return;
        }
        // submit regardless of what comes back
        else {
            clearInterval(intervalId);
            document.getElementById("Expression Report").submit();
        }
    }

</script>
