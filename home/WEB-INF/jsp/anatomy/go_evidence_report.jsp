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
                        Go Evidence Report
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
                                    <b>No Go Evidence records were found matching your query.</b><br><br>
                                </c:when>
                                <c:otherwise>
                                    <b>
                                        <zfin:choice choicePattern="0#Go Evidences| 1#Go Evidence| 2#Go Evidences"
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

<form:form method="Get" commandName="formBean" name="Report" id="Report" onsubmit="return false;">
    <c:if test="${formBean.searchResults == true}">
        <c:if test="${formBean.totalRecords > 0}">
            <table class="searchresults rowstripes">
                <tr>
                    <th width=20%>Gene</th>
                    <th width=80%>Term</th>
                </tr>
                <c:forEach var="goEvidence" items="${formBean.allGoEvidences}" varStatus="loop">
                    <zfin:alternating-tr loopName="loop">
                        <td>
                            <zfin:link entity="${goEvidence.marker}"/>
                        </td>
                        <td>
                            <zfin:link entity="${goEvidence.goTerm}"/>
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

    <link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
    <script language="javascript"
            src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>
    <table width="100%">
        <tr>
            <td width="30%">
                Enter one or more GO BP terms to find all Marker GO Evidence records.
            </td>
            <td width="70%"></td>
        </tr>
        <tr valign="top" bgcolor="#eeeeee">
            <td align="top" colspan="2">
                <b title="One or more anatomy structures that an antibody labels">GO BP Terms</b>
                <br/>
                <form:hidden path="goTermIDs"/>
                <form:hidden path="goTermNames"/>
                <script type="text/javascript">
                    var LookupProperties = {
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
                <input type="hidden" name="<%= AntibodySearchFormBean.ACTION%>"/>
            </td>
        </tr>
    </table>
</form:form>


<script type="text/javascript">

    function call_reset() {
        document.getElementById("includeSubstructuresGo1").checked = false;
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
        var form = document.getElementById("Report");
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
        document.getElementById("Report").submit();
    }

</script>
