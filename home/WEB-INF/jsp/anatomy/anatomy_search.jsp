<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/ajax-lib/prototype.js" type="text/javascript"></script>
<script src="/javascript/ajax-lib/effects.js" type="text/javascript"></script>
<script src="/javascript/ajax-lib/dragdrop.js" type="text/javascript"></script>
<script src="/javascript/ajax-lib/controls.js" type="text/javascript"></script>


<TABLE width=100%>
    <TR>
        <TD class="titlebar">
            <span style="font-size:larger; margin-left:0.5em; font-weight:bold;">
            Anatomical Ontology Browser
            </span>
        </TD>
    </TR>
</TABLE>

<tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
    <tiles:put name="subjectName" value="Anatomical Ontology Browser"/>
    <tiles:put name="subjectID" value=""/>
</tiles:insert>

<form:form method="GET" action="/action/anatomy/search" commandName="formBean" name="formBean">



<input type="hidden" name="action" value="<%= AnatomySearchBean.Action.TERM_SEARCH.toString() %>">
<TABLE width=100%>
    <TR>
        <TD>
            <TABLE border=0 width=100% cellspacing=0 cellpadding=5>
                <TR bgcolor="">
                    <TD >
                        <A HREF="/action/anatomy/search?action=<%= AnatomySearchBean.Action.COMPLETE_SEARCH.toString() %>">
                            All Anatomical Terms
                        </A>
                    </TD>
                    <TD colspan=2>
                        <A HREF="/action/anatomy/request-new-term" target="_new">
                            Request new Anatomical Term
                        </A>
                    </TD>
                </TR>
                <TR bgcolor="">
                    <TD width=45%>
                        <label for="searchTerm" class="indented-label">Anatomical Term</label><br>


                        <script language="javascript" src="/gwt/org.zfin.anatomy.presentation.AnatomyLookup/org.zfin.anatomy.presentation.AnatomyLookup.nocache.js"></script>
                        <div id="anatomyTerm"></div>
                        <%--<input type="button" value="Search" onclick="document.formBean.action.value='<%= AnatomySearchBean.Action.TERM_SEARCH.toString() %>'; document.formBean.submit();"/>--%>
                    </TD>
                    <TD width=10%>
                        <b>or</b>
                    </TD>
                    <TD width=45%>
                        <label class="namesearchLabel">
                            <a href="/zf_info/zfbook/stages/index.html">Developmental Stage</a>
                        </label><BR>
                        <form:select path="stage.zdbID" onchange="document.formBean.action.value='term-by-stage-search';
                                                            document.formBean.submit();" id="stages" htmlEscape="false">
                            <form:option value="" label="Select a Stage"/>
                            <form:options items="${formBean.displayStages}" itemLabel="key" itemValue="value"/>
                        </form:select>
                    </TD>
                </TR>
            </TABLE>

        </TD>
    </TR>
</TABLE>

<TABLE width=100%>
    <TR>
        <TD class="titlebar">
            <span style="font-size:larger; margin-left:0.5em; font-weight:bold;">
                &nbsp;&nbsp;
            </span>
            &nbsp;&nbsp;
        </TD>
    </TR>
</TABLE>

<c:if test="${formBean.termSearch}">
    <CENTER>
        <TABLE width="98%">
            <TR>
                <TD>
                    <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_list.jsp" flush="false">
                    </tiles:insert>
                </TD>
            </TR>
        </TABLE>
    </CENTER>
</c:if>

<c:if test="${formBean.completeSearch}">
    <CENTER>
        <TABLE width="98%">
            <TR>
                <TD>
                    All Anatomical Terms in alphabetical order:
                    <HR width=500 size=1 noshade align=left>
                    <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_list.jsp" flush="false">
                    </tiles:insert>
                </TD>
            </TR>
        </TABLE>
    </CENTER>
</c:if>

<c:if test="${formBean.stageSearch}">
    <TABLE width="100%">
        <tbody>
            <TR>
                <TD>
                    All
                    <c:out value="${formBean.totalRecords}"/>
                    anatomical
                    <zfin:choice integerEntity="${formBean.totalRecords}"
                                 choicePattern="0#structures| 1#structure| 2#structures" scope="Request"/>
                    at stage
                    <b>
                            ${formBean.currentDisplayStageString}
                    </b>
                </TD>
                <td>
                    <FONT size=2> Highlight terms containing: </FONT> <BR>
                    <form:input path="highlightText" size="20"
                                onchange="document.formBean.action.value='term-by-stage-search'; document.formBean.submit();"/>
                </TD>
            </TR>
        </tbody>
        <tr>
            <td colspan="2">
                <HR width=100% size=1 noshade align=left>
            </td>
        </tr>
        <TR>
            <TD>
                <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_list.jsp" flush="false">
                </tiles:insert>
            </TD>
        </TR>
    </TABLE>
</c:if>
</form:form>




