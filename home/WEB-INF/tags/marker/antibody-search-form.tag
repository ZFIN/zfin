<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ tag import="org.zfin.util.FilterType" %>
<%@ tag import="org.zfin.framework.presentation.PaginationBean" %>
<%@ tag import="org.zfin.antibody.AntibodyType" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.antibody.presentation.AntibodySearchFormBean" required="true" %>

<form:form method="Get" action="antibody-do-search" modelAttribute="formBean" name="Antibody Search"
           id="Antibody Search" onsubmit="return false;">

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
            <td></td>
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
            <td></td>
        </tr>
        <tr valign="top" bgcolor="#eeeeee">
            <td align="top" colspan="2">
                <b title="One or more anatomy structures that an antibody labels">Labeled Anatomy</b>
                <br/>

                <form:hidden path="antibodyCriteria.anatomyTermIDs"/>
                <form:hidden path="antibodyCriteria.anatomyTermNames"/>
                <script type="text/javascript">
                    var LookupProperties = {
                        divName: "anatomyTerm",
                        inputName: "searchTerm",
                        showError: true,
                        <c:if test='${formBean.antibodyCriteria.anatomyTermNames != null}' >
                        previousTableValues: "${formBean.antibodyCriteria.anatomyTermNames}",
                        </c:if>
                        hiddenNames: "antibodyCriteria.anatomyTermNames",
                        hiddenIds: "antibodyCriteria.anatomyTermIDs",
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
                            <form:checkbox path="antibodyCriteria.includeSubstructures"/>
                            <label for="antibodyCriteria.includeSubstructures" class="accessoryLabel">Include
                                substructures</label>
                        </td>
                        <td>
                            <form:radiobutton path="antibodyCriteria.anatomyEveryTerm" value="true"/>
                            <label for="antibodyCriteria.anatomyEveryTerm" class="accessoryLabel"> <b>Every</b> term
                                entered
                                &nbsp;<br></label>
                            <form:radiobutton path="antibodyCriteria.anatomyEveryTerm" value="false"/>
                            <label for="antibodyCriteria.anatomyEveryTerm2" class="accessoryLabel"> <b>Any</b> term
                                entered
                            </label>
                        </td>
                    </tr>
                </table>

            </td>
            <td align="left">
                <b>Between stages:</b><br/>
                <form:select path="antibodyCriteria.startStage.zdbID" id="startStage">
                    <form:options items="${formBean.displayStages}"/>
                </form:select>
                &nbsp;<b>&</b>
                <br/>
                <form:select path="antibodyCriteria.endStage.zdbID" id="endStage">
                    <form:options items="${formBean.displayStages}"/>
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
                                <%--<form:options items="${formBean.assayList}" itemLabel="value" itemValue="key"/>--%>
                                <form:options items="${formBean.assayList}"/>
                            </form:select>
                        </td>
                    </tr>
                </table>
            </td>
            <td align="left">
                <form:radiobutton path="antibodyCriteria.clonalType" value="<%= AntibodyType.MONOCLONAL.getValue()%>" autocomplete="off"/>
                <label for="antibodyCriteria.clonalType">Monoclonal</label>
                &nbsp; <br/>
                <form:radiobutton path="antibodyCriteria.clonalType" value="<%= AntibodyType.POLYCLONAL.getValue()%>" autocomplete="off"/>
                <label for="antibodyCriteria.clonalType">Polyclonal </label> &nbsp;<br/>
                <form:radiobutton path="antibodyCriteria.clonalType" value="<%= AntibodyType.ANY.getValue()%>" autocomplete="off"/>
                <label for="antibodyCriteria.clonalType">Both</label>
                Types
            </td>
        </tr>
        <tr>
            <td colspan="2"/>
            <td align="left">
                &nbsp;<br/>
                <form:radiobutton path="antibodyCriteria.zircOnly" value="true" autocomplete="off"/>
                <label for="antibodyCriteria.zircOnly">Show only ZIRC Antibodies</label> <br/>
                <form:radiobutton path="antibodyCriteria.zircOnly" value="false" autocomplete="off"/>
                <label for="antibodyCriteria.zircOnly">Show All</label>
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
            <td class="submitbar" bgcolor="#cccccc">
                <input value="Search" onclick="submitForm(1)" type="button">
                <input value="Reset" type="button" onclick="call_reset()">
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
        var pauseIcrement = 500;  // wait up to 4 seconds
        var currentTime = new Date();  // wait up to 4 seconds
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
            document.getElementById("Antibody Search").submit();
        }
    }

</script>
