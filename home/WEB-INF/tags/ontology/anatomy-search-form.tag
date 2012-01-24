<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.AnatomySearchBean" required="true" %>

<table class="search" width=100% border="0" cellspacing="0">
    <tr>
        <td class="titlebar">
            <span style="font-size:larger; margin-left:0.5em; font-weight:bold;">
            Anatomical Ontology Browser
            </span>
        </td>
        <td class="titlebar" align="right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Anatomical Ontology Browser"/>
                <tiles:putAttribute name="subjectID" value=""/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>


<TABLE width=100%>
    <TR>
        <TD>
            <TABLE border=0 width=100% cellspacing=0 cellpadding=5>
                <TR bgcolor="">
                    <TD>
                        <A HREF="/action/anatomy/show-all-anatomy-terms">
                            All Anatomical Terms
                        </A>
                    </TD>
                    <TD colspan=2>
                        <A HREF="/action/anatomy/request-new-anatomy-term" target="_new">
                            Request new Anatomical Term
                        </A>
                    </TD>
                </TR>
                <TR bgcolor="">
                    <TD width=45%>
                            <label class="indented-label">Anatomical Term</label><br>
                            <zfin2:lookup ontology="<%=Ontology.ANATOMY%>"
                                          action="<%= LookupComposite.ACTION_ANATOMY_SEARCH%>"
                                          wildcard="true" useIdAsTerm="false"/>
                    </TD>
                    <TD width=10%>
                        <span class="bold">or</span>
                    </TD>
                    <TD width=45%>
                        <label class="namesearchLabel">
                            <a href="/zf_info/zfbook/stages/index.html">Developmental Stage</a>
                        </label><BR>
                        <form:form method="GET" action="/action/anatomy/show-terms-by-stage" commandName="formBean"
                                   name="formBean">
                            <form:select path="stage.zdbID" onchange="document.formBean.submit();" id="stages"
                                         htmlEscape="false">
                                <form:option value="" label="Select a Stage"/>
                                <form:options items="${formBean.displayStages}"/>
                            </form:select>
                        </form:form>
                    </TD>
                </TR>
            </TABLE>

        </TD>
    </TR>
</TABLE>

