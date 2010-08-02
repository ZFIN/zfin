<%@ page import="org.zfin.antibody.AntibodyType" %>
<%@ page import="org.zfin.antibody.presentation.AntibodySearchFormBean" %>
<%@ page import="org.zfin.util.FilterType" %>
<%@ page import="org.zfin.framework.presentation.PaginationBean" %>
<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.gwt.lookup.ui.Lookup" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" scope="request"/>

<c:choose>
    <c:when test="${formBean.searchResults == false}">
        <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
                <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        Expression  Report
            </span>
                </td>
            </tr>
        </table>
    </c:when>
    <c:otherwise>
        <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
                <td colspan="6" align="right"><a href="#modify-search">Modify Search</a></td>
            </tr>
            <tr>
                <td>
                    <c:if test="${formBean.searchResults == true}">
                        <div align="center">
                            <c:choose>
                                <c:when test="${formBean.totalRecords == 0}">
                                    <b>No Expression records were found matching your query.</b><br><br>
                                </c:when>
                                <c:otherwise>
                                    <b>
                                        <zfin:choice choicePattern="0#Expressions| 1#Expression| 2#Expressions"
                                                     integerEntity="${formBean.totalRecords}" includeNumber="true"/>
                                    </b>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
            </tr>
        </table>
    </c:otherwise>
</c:choose>

<form:form method="Get" commandName="formBean" name="Expression Report" id="Expression Report" onsubmit="return false;">
    <c:if test="${formBean.searchResults == true}">
        <c:if test="${formBean.totalRecords > 0}">
            <table class="searchresults rowstripes">
                <tr>
                    <th width=5%>Gene</th>
                    <th width=5%>Antibody</th>
                    <th width=20%>Fish</th>
                    <th width=10%>Environment</th>
                    <th width=10%>Assay</th>
                    <th width=30%>Structure</th>
                    <th width=15%>Start Stage</th>
                    <th width=15%>End Stage</th>
                </tr>
                <c:forEach var="phenotype" items="${formBean.allExpressions}" varStatus="loop">
                    <zfin:alternating-tr loopName="loop">
                        <td>
                            <zfin:link entity="${phenotype.expressionExperiment.gene}"/>
                        </td>
                        <td>
                            <zfin:link entity="${phenotype.expressionExperiment.antibody}"/>
                        </td>
                        <td>
                                ${phenotype.expressionExperiment.genotypeExperiment.genotype.name}
                        </td>
                        <td>
                                ${phenotype.expressionExperiment.genotypeExperiment.experiment.name}
                        </td>
                        <td>
                                ${phenotype.expressionExperiment.assay.abbreviation}
                        </td>
                        <td>
                               <zfin:link entity="${phenotype.superterm}"/>
                            <c:if test="${phenotype.subterm ne null}"> :
                            <zfin:link entity="${phenotype.subterm}"/></c:if>
                        </td>
                        <td>
                                ${phenotype.startStage.abbreviation}
                        </td>
                        <td>
                                ${phenotype.endStage.abbreviation}
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>

            <input name="page" type="hidden" value="1" id="page"/>
            <zfin2:pagination paginationBean="${formBean}"/>
        </c:if>
    </c:if>

    <c:if test="${formBean.searchResults == true}">
        <p></p>
        <table width="100%">
            <tr>
                <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        <a name="modify-search">Modify your search </a>
            </span>
                    &nbsp;&nbsp; <a href="javascript:start_tips();">Search Tips</a>
                </td>
            </tr>
        </table>
    </c:if>

    <table width="100%" class="error-box">
        <tr>
            <td>
                <form:errors path="*" cssClass="Error"/>
            </td>
        </tr>
    </table>

    <table width="100%">
        <tr>
            <td width="30%" colspan="2">
                Select all the anatomy structure for which you would like to find expression annotations.
                (Note: including substructures currently includes the develops_from relationship as well.)
            </td>
            <td width="70%">
            </td>
        </tr>
        <tr valign="top" bgcolor="#eeeeee">
            <td align="top" colspan="3">
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
                        useTermTable: true,
                    <%= Lookup.JSREF_SHOW_TERM_DETAIL%>:
                    false,
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
                <input type="hidden" name="<%= AntibodySearchFormBean.ACTION%>"/>
            </td>
        </tr>
    </table>
</form:form>


<script type="text/javascript">

    function call_reset() {
        document.getElementById("maxDisplayRecords").value = <%= PaginationBean.MAX_DISPLAY_RECORDS_DEFAULT %>;
        document.getElementById("includeSubstructures1").checked = true;
        document.getElementById("anatomyEveryTerm1").checked = true;
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
        form.action.value = "<%= AntibodySearchFormBean.Type.SEARCH.toString()%>";
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
            clearInterval(intervalId);
            document.getElementById("Expression Report").submit();
    }

</script>
