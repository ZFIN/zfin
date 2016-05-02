<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<TABLE width="100%">
    <tbody>
    <TR>
        <TD>
            All
            <c:out value="${fn:length(formBean.statisticItems)}"/>
            anatomical
            <zfin:choice integerEntity="${fn:length(formBean.statisticItems)}"
                         choicePattern="0#structures| 1#structure| 2#structures" scope="Request"/>
            at stage
            <b>
                ${formBean.currentDisplayStageString}
            </b>
        </TD>
        <td>
            Highlight terms containing: <BR>
            <form:form method="GET" action="/action/ontology/show-anatomy-terms-by-stage" commandName="formBean"
                       id="termsByStage">
                <form:input path="highlightText" size="20" onchange="submit();"/>
                <form:hidden path="stage.zdbID"/>
            </form:form>
        </TD>
        <td></td>
    </TR>
    </tbody>
    <tr>
        <td colspan="3">
            <HR width=100% size=1 noshade align=left>
        </td>
    </tr>
    <TR>
        <TD colspan="2">
            <tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_list.jsp" flush="false"/>
        </TD>
    </TR>
</TABLE>