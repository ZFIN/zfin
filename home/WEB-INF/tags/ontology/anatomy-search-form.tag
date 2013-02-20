<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.AnatomySearchBean" required="true" %>

<table class="search" width=100% border="0" cellspacing="0">
    <tr>
        <td class="titlebar">
            <span style="font-size:larger; margin-left:0.5em; font-weight:bold;">
            Ontology Search
            </span>
        </td>
        <td style="background-color: #efefef; text-align: right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Ontology Search"/>
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
                    <TD width="50">
                    </TD>
                    <TD>
                    </TD>
                    <TD rowspan="4">
                        <A HREF="/action/anatomy/request-new-anatomy-term" target="_new">
                            Request a new Anatomical Term
                        </A>

                        <p/>
                        <A HREF="/action/ontology/show-all-anatomy-terms">
                            Display all Anatomical Terms
                        </A>

                        <p/>
                        Browse Anatomy Terms by
                        <a href="/zf_info/zfbook/stages/index.html">Developmental Stage</a>
                        <BR>
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
                <tr>
                    <td>&nbsp;</td>
                </tr>
                <TR>
                    <TD width="40" valign="top" align="right">
                        <label class="indented-label">Term:</label><br>
                    </TD>
                    <TD valign="top">
                        <span id="aogo">
                        <zfin2:lookup ontology="<%=Ontology.AOGO%>"
                                      action="<%= LookupComposite.ACTION_ANATOMY_SEARCH%>"
                                      wildcard="true" useIdAsTerm="false" termsWithDataOnly="false"/>
                            </span>
                    </TD>
                </TR>
                <TR>
                    <TD valign="top" align="right" style="font-size: 12px">
                    <TD valign="top" style="font-size: 12px">
                        <span id="ontologyChecker"></span> Anatomy terms only <p/>
                    </TD>
                </TR>
            </TABLE>

        </TD>
    </TR>
</TABLE>

