<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.anatomy.AnatomyItem,
                 org.zfin.anatomy.DevelopmentStage" %>
<%@ page import="org.zfin.anatomy.presentation.StagePresentation" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td><font size="-1"><b>ZFIN ID:</b>
                ${formBean.anatomyItem.zdbID}
            </font>&nbsp;
                <authz:authorize ifAnyGranted="root">
                    <A HREF='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-update-vframeset.apg&OID=${formBean.anatomyItem.zdbID}&rtype=anatomy_item'>
                        Last Update:
                        <c:choose>
                            <c:when test="${formBean.latestUpdate != null}">
                                <fmt:formatDate value="${formBean.latestUpdate.dateUpdated}" type="date"/>
                            </c:when>
                            <c:otherwise>Never modified</c:otherwise>
                        </c:choose>
                    </A> &nbsp;
                </authz:authorize>
            </td>
        </tr>
    </tbody>
</table>

<tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
    <tiles:put name="subjectName" value="${formBean.anatomyItem.name}"/>
    <tiles:put name="subjectID" value="${formBean.anatomyItem.zdbID}"/>
</tiles:insert>

<p/>
<table width="100%">
    <tr>
        <td width="80">
            <FONT SIZE=+1><STRONG>Name:</STRONG></FONT></td>
        <td>
            <FONT SIZE=+1><STRONG>
                ${formBean.anatomyItem.name}
            </STRONG></FONT>
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
<p>
<TABLE width="100%">
    <TR>
        <TD bgcolor="#CCCCCC">
            <STRONG>Appears at</STRONG>
        </TD>
        <TD bgcolor="#CCCCCC">
            <STRONG>Evident until</STRONG>
        </TD>
    </TR>
    <TR>
        <%
            AnatomySearchBean formBean = (AnatomySearchBean) request.getAttribute("formBean");
            AnatomyItem ai = formBean.getAnatomyItem();
            String startStage = StagePresentation.createDisplayEntry(ai.getStart());
            String endStage = StagePresentation.createDisplayEntry(ai.getEnd());
            String startAnchor = ai.getStart().abbreviation();
            String endAnchor = ai.getEnd().abbreviation();
        %>
        <TD bgcolor="#EEEEEE">
            <% if (startAnchor != null && !startAnchor.equals(DevelopmentStage.UNKNOWN)) { %>
            <a href="/zf_info/zfbook/stages/index.html#<%= startAnchor%>">
                <%= startStage %>
            </a>
            <% } else { %>
            <%= startStage %>
            <% } %>
        </TD>
        <TD bgcolor="#EEEEEE">
            <% if (endAnchor != null && !endAnchor.equals(DevelopmentStage.UNKNOWN)) { %>
            <a href="/zf_info/zfbook/stages/index.html#<%= endAnchor%>">
                <%= endStage %>
            </a>
            <% } else { %>
            <%= endStage %>
            <% } %>
        </TD>
    </TR>
</TABLE>

<p>
    <!-- Relationships -->
<TABLE width="100%">
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
                    <zfin:link entity="${item}"/>
                    &nbsp;
                </c:forEach>
            </TD>
        </TR>

    </c:forEach>
</TABLE>

</p>

<hr width="80%">

<p/>
<b>EXPRESSION</b> <br>
<b>
    <c:if test="${formBean.expressedGenesExist}">
        <div title="Genes with Most Figures, annotated to ${formBean.anatomyItem.name}, substructures excluded">Genes
            with
            Most Figures
        </div>
        <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_term_detail_expressed_genes.jsp" flush="false"/>
    </c:if>
</b>

<p/>

<p/>
<!-- In situ Probes -->
<c:if test="${formBean.inSituProbesExist}">

    <b>In Situ Probes</b>: <a href="/zf_info/stars.html"> Recommended </a> by
    <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-labview.apg&OID=ZDB-LAB-980204-15'>
        Thisse lab</a>
    <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_term_detail_insitu_probes.jsp" flush="false"/>
</c:if>
<hr width="80%">
<p/>
<!-- All mutants -->
<c:if test="${formBean.mutantsExist ||formBean.morpholinoExist }">
    <b>PHENOTYPE</b>
    <br/>
    <c:if test="${formBean.mutantsExist}">
        <b>Mutant and Transgenic Lines</b>
        <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_term_detail_mutants.jsp" flush="false"/>
    </c:if>
    <p/>
    <c:if test="${formBean.morpholinoExist}">
        <b>Morpholinos</b>
        <tiles:insert page="/WEB-INF/jsp/anatomy/anatomy_term_detail_morpholinos.jsp" flush="false"/>
    </c:if>
    <hr width="80%">
</c:if>
<!-- Number of Publications with an abstract that contains the anatomical structure -->

<c:if test="${formBean.numberOfPublications > 0}">
    <STRONG>
        (<A HREF='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubselect2.apg&anon1=pub_abstract&anon1text=${formBean.anatomyItem.name}&anon1textAllOneWord=1&query_results=exists'>${formBean.numberOfPublications}
    </A>)
        <zfin:choice choicePattern="0#Publications| 1#Publication| 2#Publications"
                     integerEntity="${formBean.numberOfPublications}"/>
    </STRONG> with "<i>
    ${formBean.anatomyItem.name}
    </i>" occurring in the abstract.
</c:if>
