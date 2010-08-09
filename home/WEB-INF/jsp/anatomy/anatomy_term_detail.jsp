<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin2:dataManager zdbID="${formBean.anatomyItem.zdbID}"
                   oboID="${formBean.anatomyItem.oboID}"
                   termID="${formBean.aoTerm.ID}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="anatomy_item"/>

<div style="float: right;">
    <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:put name="subjectName" value="${formBean.anatomyItem.name}"/>
        <tiles:put name="subjectID" value="${formBean.anatomyItem.zdbID}"/>
    </tiles:insert>
</div>
<div class="summary">
    <table width="100%">
        <tr>
            <td width="80">
                <FONT SIZE=+1><STRONG>Name:</STRONG></FONT></td>
            <td>
                <FONT SIZE=+1><STRONG>
                    ${formBean.anatomyItem.name}
                </STRONG></FONT>
                <c:if test="${formBean.anatomyItem.obsolete}"><span style="color:red">(obsolete)</span> </c:if>
            </td>
            <td valign="top" align="right" width="5%">
                Search:
            </td>
            <td rowspan="3" valign="top" align="right" width="5%">
                <zfin2:lookup ontology="<%= Ontology.ANATOMY%>"
                              action="<%= LookupComposite.ACTION_ANATOMY_SEARCH %>" showTermDetail="false"
                              wildcard="true"/>
            </td>
        </tr>
        <c:if test="${formBean.anatomyItem.synonyms != null  }">
            <tr valign="top">
                <td>
                    Synonyms:
                </td>
                <td>
                        ${formBean.formattedSynonymList}
                </td>
            </tr>
        </c:if>

        <c:if test="${formBean.anatomyItem.definition != null  }">
            <tr valign="top">
                <td>
                    Definition:
                </td>
                <td>
                        ${formBean.anatomyItem.definition}
                </td>
            </tr>
        </c:if>
    </table>
</div>

<div class="summary">
    <TABLE width="100%">
        <TR>
            <TD bgcolor="#CCCCCC" width="50%">
                <STRONG>Appears at</STRONG>
            </TD>
            <TD bgcolor="#CCCCCC">
                <STRONG>Evident until</STRONG>
            </TD>
        </TR>
        <TR>
            <TD bgcolor="#EEEEEE">
                <zfin:link entity="${formBean.anatomyItem.start}" longVersion="true"/>
            </TD>
            <TD bgcolor="#EEEEEE">
                <zfin:link entity="${formBean.anatomyItem.end}"/>
            </TD>
        </TR>
    </TABLE>
</div>

<div class="summary">
    <!-- Relationships -->
    <TABLE width="100%" class="summary">
        <TR>
            <TD bgcolor="#CCCCCC" colspan=2>
                <STRONG>Relationships</STRONG>
                (<a href="/zf_info/ontology_relationship_info.html">about</a>)
            </TD>
        </TR>
        <c:forEach var="rt" items="${formBean.relations}">
            <TR>
                <TD bgcolor="#EEEEEE" valign=top align=left width=110>
                        ${rt.type}:
                </TD>
                <TD bgcolor="#EEEEEE">
                    <c:forEach var="item" items="${rt.items}">
                        <zfin:link entity="${item}" name="anatomy-visibility"/> &nbsp;
                        &nbsp;
                    </c:forEach>
                </TD>
            </TR>
        </c:forEach>
    </TABLE>
</div>

<hr width="80%">

<%--   If you would like to display the Show All link 
<zfin2:sectionVisibilityShowAll sectionVisibility="${formBean.sectionVisibility}"
                                enumeration="${formBean.sectionVisibility.sectionsWithData}"/>
<p/>
--%>
<tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_term_detail_expression.jsp" flush="false"/>

<tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_term_detail_phenotype.jsp" flush="false"/>

<zfin2:ExpandRequestSections sectionVisibility="${formBean.sectionVisibility}"/>

<hr width="80%">
<div class="summary">
    <%// Number of Publications with an abstract that contains the anatomical structure %>
    <A HREF='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubselect2.apg&anon1=pub_abstract&anon1text=${formBean.anatomyItem.name}&anon1textAllOneWord=1&query_results=exists'>Search
        for publications with '${formBean.anatomyItem.name}' in abstract</A>
</div>

