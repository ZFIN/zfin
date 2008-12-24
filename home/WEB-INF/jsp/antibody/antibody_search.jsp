<%@ page import="org.zfin.antibody.AntibodyType" %>
<%@ page import="org.zfin.antibody.presentation.AntibodySearchFormBean" %>
<%@ page import="org.zfin.util.FilterType" %>
<%@ page import="org.zfin.framework.presentation.PaginationBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${formBean.searchResults == false}">
        <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
                <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        Search for Antibodies
            </span>
                    &nbsp;&nbsp;
                    <c:import url="/WEB-INF/jsp/antibody/antibody_search_tip.jsp"/>
                </td>
                <td align="right" class="titlebar">
                    <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                        <tiles:put name="subjectName" value="Antibody search"/>
                        <tiles:put name="subjectID" value=""/>
                    </tiles:insert>
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
                <td col="">
                    <c:if test="${formBean.searchResults == true}">
                        <div align="center">
                            <c:choose>
                                <c:when test="${formBean.totalRecords == 0}">
                                    <b>No antibodies were found matching your query.</b><br><br>
                                </c:when>
                                <c:otherwise>
                                    <b>
                                        <zfin:choice choicePattern="0#Antibodies| 1#Antibody| 2#Antibodies"
                                                     integerEntity="${formBean.totalRecords}" includeNumber="true"/>
                                        </b>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </td>
                <td align="right"  width="80">
                    <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                        <tiles:put name="subjectName" value="Antibody search"/>
                        <tiles:put name="subjectID" value=""/>
                    </tiles:insert>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
            </tr>
        </table>
    </c:otherwise>
</c:choose>

<form:form method="Get" commandName="formBean" name="Antibody Search" id="Antibody Search" onsubmit="return false;">
<c:if test="${formBean.searchResults == true}">
    <c:if test="${formBean.totalRecords > 0}">
        <table width=100% border=0 cellspacing=0>
            <tr align=left>
                <td align=left width=10% valign="top">
                    <b>Name</b>
                </td>
                <td align=left width=10% valign="top"><b>Gene</b></td>
                <td align=left width=25% valign="top"><b>Anatomy</b></td>
                <td align=left width=20% valign="top"><b>Cellular Components</b></td>
                <td align=left width="15%" valign="top"><b>Stage Range</b></td>
                <c:if test="${formBean.matchingTextSearch}">
                    <td align=left><b>Matching Text</b></td>
                </c:if>
            </tr>
            <c:forEach var="antibodyStat" items="${formBean.antibodyStats}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td valign=top>
                        <zfin:link entity="${antibodyStat.antibody}"/>
                    </td>
                    <td valign="top">
                        <c:forEach var="antigenRel" items="${antibodyStat.sortedAntigenRelationships}"
                                   varStatus="relIndex">
                            <zfin:link entity="${antigenRel.firstMarker}"/><c:if test="${!relIndex.last}">, </c:if>
                        </c:forEach>
                    </td>
                    <td valign="top">
                        <zfin2:toggledHyperlinkList collection="${antibodyStat.distinctAnatomyTerms}" maxNumber="3"
                                                    id="${antibodyStat.antibody.zdbID}"/>
                    </td>
                    <td valign=top>
                        <c:forEach var="goTerm" items="${antibodyStat.distinctGoTermsWTAndStandard}" varStatus="index">
                            <zfin:link entity="${goTerm}"/>
                            <c:if test="${!index.last}">, </c:if>
                        </c:forEach>
                    </td>
                    <td valign=top>
                        <zfin:link entity="${antibodyStat.earliestStartStage}"/>
                        <c:if test="${antibodyStat.earliestStartStage != antibodyStat.latestEndStage}">
                            to <zfin:link entity="${antibodyStat.latestEndStage}"/>
                        </c:if>
                    </td>
                    <c:if test="${formBean.matchingTextSearch}">
                        <td valign=top>
                            <c:choose>
                                <c:when test="${antibodyStat.matchingText == null || fn:length(antibodyStat.matchingText) == 1}">
                                    <c:forEach var="matchingTerm" items="${antibodyStat.matchingText}" varStatus="loop">
                                        ${matchingTerm.descriptor}:
                                        <zfin:hightlight highlightEntity="${matchingTerm.matchingString}"
                                                         highlightStrings="${matchingTerm.matchedStrings}"/> ${matchingTerm.appendix}
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <ul>
                                        <c:forEach var="matchingTerm" items="${antibodyStat.matchingText}"
                                                   varStatus="loop">
                                            <li>${matchingTerm.descriptor}:
                                                <zfin:hightlight highlightEntity="${matchingTerm.matchingString}"
                                                                 highlightStrings="${matchingTerm.matchedStrings}"/>
                                                    ${matchingTerm.appendix}
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:if>
                </zfin:alternating-tr>
            </c:forEach>
        </table>

        <input name="page" type="hidden" value="1" id="page"/>
        <zfin2:pagination paginationBean="${formBean}" />
    </c:if>
</c:if>

<c:if test="${formBean.searchResults == true}">
    <p/>
    <table width="100%">
        <tr>
            <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        <a name="modify-search"/>Modify your search
            </span>
                &nbsp;&nbsp;
                <c:import url="/WEB-INF/jsp/antibody/antibody_search_tip.jsp"/>
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
    <td width="15%">
    </td>
    <td width="35%">
    </td>
    <td width="50%">
    </td>
</tr>
<tr>
    <td>
        <b title="Antibody name or alias">Antibody Name:</b>
    </td>
    <td>
        <form:select path="antibodyCriteria.antibodyNameFilterType" id="antibodyNameFilterType"
                     items="${formBean.antibodyNameFilterTypeList}"/> &nbsp;
        <form:input path="antibodyCriteria.name" size="30"
                    onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { submitForm(1);}  "/>
    </td>
    <td/>
</tr>
<tr valign="top">
    <td align="top">
        <b title="Name or previous name of a gene whose products the antibody recognizes">Antigen Gene:</b>
    </td>
    <td>
        <form:select path="antibodyCriteria.antigenNameFilterType" id="antigenNameFilterType"
                     items="${formBean.antigenNameFilterTypeList}"/> &nbsp;
        <form:input path="antibodyCriteria.antigenGeneName" size="30"
                    onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { submitForm(1);}  "/>
    </td>
    <td/>
</tr>
<tr valign="top" bgcolor="#eeeeee">
    <td align="top" colspan="2">
        <b title="One or more anatomy structures that an antibody labels">Labeled Anatomy</b>
        <br/>

        <form:hidden path="antibodyCriteria.anatomyTermsString"/>
        <script type="text/javascript">
            var LookupProperties = {
                divName: "anatomyTerm",
                inputName: "searchTerm",
                showError: true,
                imageURL: "/gwt/org.zfin.framework.presentation.LookupTable/",
                <c:if test='${formBean.antibodyCriteria.anatomyTermsString != null}' >
                previousTableValues: "${formBean.antibodyCriteria.anatomyTermsString}",
                </c:if>
                hiddenName: "antibodyCriteria.anatomyTermsString",
                type: "ANATOMY_ONTOLOGY",
                width: 40,
                wildcard: false
            };
        </script>

        <style type="text/css">
            .accessoryLabel {
                font-size: .75em;
            }
        </style>

        <link rel="stylesheet" type="text/css" href="/gwt/org.zfin.framework.presentation.LookupTable/Lookup.css"/>
        <script language="javascript"
                src="/gwt/org.zfin.framework.presentation.LookupTable/org.zfin.framework.presentation.LookupTable.nocache.js"></script>

        <div id="anatomyTerm"></div>
        <table>
            <tr>
                <td valign="top" width="60%">
                    <form:checkbox path="antibodyCriteria.includeSubstructures"/>
                    <label for="antibodyCriteria.includeSubstructures1" class="accessoryLabel">Include
                        substructures</label>
                </td>
                <td>
                    <form:radiobutton path="antibodyCriteria.anatomyEveryTerm" value="true"/>
                    <label for="antibodyCriteria.anatomyEveryTerm1" class="accessoryLabel"> <b>Every</b> term entered
                        &nbsp;<br></label>
                    <form:radiobutton path="antibodyCriteria.anatomyEveryTerm" value="false"/>
                    <label for="antibodyCriteria.anatomyEveryTerm2" class="accessoryLabel"> <b>Any</b> term entered
                    </label>
                </td>
            </tr>
        </table>

    </td>
    <td align="left">
        <b>Between stages:</b><br/>
        <form:select path="antibodyCriteria.startStage.zdbID" id="startStage">
            <form:options items="${formBean.displayStages}" itemLabel="key" itemValue="value"/>
        </form:select>
        &nbsp;<b>&</b>
        <br/>
        <form:select path="antibodyCriteria.endStage.zdbID" id="endStage">
            <form:options items="${formBean.displayStages}" itemLabel="key" itemValue="value"/>
        </form:select>
        <br/>

        <div align="left">
            <a href="http://zfin.org/zf_info/zfbook/stages/index.html">Developmental Staging Series</a>
        </div>
    </td>
</tr>
<tr>
    <td colspan="3">
    </td>
</tr>
<tr valign="top">
    <td valign="top" colspan="2">
        <table>
            <tr>
                <td nowrap="nowrap">
                    <b title="Organisms in which the antibody was created. List contains only organisms that are actually used">
                        Host Organism:</b>
                </td>
                <td>
                    <form:select path="antibodyCriteria.hostSpecies" multiple="single"
                                 items="${formBean.antigenOrganismList}"/>
                </td>
            </tr>
            <tr>
                <td>
                    <b> Assay:</b>
                </td>
                <td>
                    <form:select path="antibodyCriteria.assay" multiple="single">
                        <form:options items="${formBean.assayList}" itemLabel="value" itemValue="key"/>
                    </form:select>
                </td>
            </tr>
        </table>
    </td>
    <td align="left">
        <form:radiobutton path="antibodyCriteria.clonalType" value="<%= AntibodyType.MONOCLONAL.getName()%>"/>
        <label for="antibodyCriteria.clonalType1">Monoclonal</label>
        &nbsp; <br/>
        <form:radiobutton path="antibodyCriteria.clonalType" value="<%= AntibodyType.POLYCLONAL.getName()%>"/>
        <label for="antibodyCriteria.clonalType2">Polyclonal </label> &nbsp;<br/>
        <form:radiobutton path="antibodyCriteria.clonalType" value="<%= AntibodyType.ANY.getName()%>"/>
        <label for="antibodyCriteria.clonalType3">Both</label>
        Types
    </td>
</tr>
<tr>
    <td colspan="2"/>
    <td align="left">
        &nbsp;<br/>
        <form:radiobutton path="antibodyCriteria.zircOnly" value="true"/>
        <label for="antibodyCriteria.zircOnly1">Show only ZIRC Antibodies</label> <br/>
        <form:radiobutton path="antibodyCriteria.zircOnly" value="false"/>
        <label for="antibodyCriteria.zircOnly2">Show All</label>
    </td>
</tr>
<tr valign="top">
    <td colspan="2">
    </td>
    <td align="left" colspan="2">
        &nbsp;<br/>
        <form:input path="maxDisplayRecords" size="4"
                    onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { submitForm(1);}  "/>
        results per page
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
        document.getElementById("antibodyNameFilterType").value = '<%= FilterType.CONTAINS.getName()%>';
        document.getElementById("antibodyCriteria.name").value = '';
        document.getElementById("antibodyCriteria.antigenGeneName").value = '';
        document.getElementById("antibodyCriteria.hostSpecies").value = 'Any';
        document.getElementById("antibodyCriteria.assay").value = 'Any';
        document.getElementById("antibodyCriteria.clonalType3").checked = true;
        document.getElementById("antibodyCriteria.zircOnly2").checked = true;
        document.getElementById("maxDisplayRecords").value = <%= PaginationBean.MAX_DISPLAY_RECORDS_DEFAULT %>;
        document.getElementById("startStage").selectedIndex = 0;
        document.getElementById("endStage").selectedIndex = document.getElementById("endStage").options.length - 1;
        document.getElementById("antibodyCriteria.includeSubstructures1").checked = true;
        document.getElementById("antibodyCriteria.anatomyEveryTerm1").checked = true;
        // clear the ao term table
        clearTable();
    }

    // populate the G-Ref field with the zdb id from the curator publication list.
    function populateGRef(curPub, GRef) {
        var zdbID = document.getElementById(curPub).value;
        if (zdbID != 'Select Curator Pub')
            document.getElementById(GRef).value = zdbID;
    }

    // set the end stage field to the same value as
    // the start field value
    // passed in are the ids
    function setEndStage(start, end) {
        var startStage;
        startStage = document.getElementById(start).value;
        document.getElementById(end).value = startStage;
    }

    function markAsChanged(element) {
        document.getElementById(element).style.color = 'red';
    }

    function submitForm(page) {
        var pauseIcrement = 500 ;  // wait up to 4 seconds
        var currentTime = new Date() ;  // wait up to 4 seconds
        try {
            validateLookup();
        }
        catch(e) {
            // if not defined, then keep going
        }
        var form = document.getElementById("Antibody Search");
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
        var maxTime = 5000 ;
        var diff = (new Date()).getMilliseconds() - currentTime;
        var inputElement = document.getElementById(LookupProperties.inputName);
        termStatus = getValidationStatus();

        // either there was an asynchronous connection failure, or we are still waiting,
        // in which case we should resubmit the query
        if (termStatus == "TERM_STATUS_LOOKING" || termStatus == "TERM_STATUS_FAILURE" && diff < maxTime) {
            validateLookup();
            return;
        }
        // submit regardless of what comes back
        else {
            clearInterval(intervalId);
            document.getElementById("Antibody Search").submit();
        }
    }

</script>
