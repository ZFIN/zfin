<%@ tag import="org.zfin.ontology.presentation.OntologyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" required="true" %>
<%@ attribute name="action" type="java.lang.String" required="true" %>

<h2>List of
    <c:choose>
        <c:when test="${action eq '<%=OntologyBean.ActionType.SHOW_OBSOLETE_TERMS%>'}">
        Obsolete Terms
        </c:when>
    <c:when test="${action eq '<%=OntologyBean.ActionType.SHOW_ALIASES%>'}">
        Obsolete Terms
        </c:when>
    <c:when test="${action eq '<%=OntologyBean.ActionType.SHOW_EXACT%>'}">
        Exact Terms
        </c:when>
    <c:otherwise>
        All
    </c:otherwise>
    </c:choose>
    Terms for [${formBean.ontologyName}] ontology</h2>

Total of: ${fn:length(formBean.terms)}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <td width="50" class="sectionTitle">ID</td>
        <td width="300" class="sectionTitle">Term Name</td>
        <td class="sectionTitle">Obo ID</td>
        <td class="sectionTitle">Term ZdbID</td>
        <td class="sectionTitle">Properties (Obs/Alias)</td>
    </tr>

    <c:forEach var="value" items="${formBean.terms}" varStatus="loop">
        <c:if test="${
        (action eq 'SHOW_OBSOLETE_TERMS'  && value.obsolete)
         ||
        (action eq 'SHOW_ALIASES'  && value.aliasesExist)
        ||
        (action eq 'SHOW_ALL_TERMS')
        }">
        <tr class="search-result-table-entries left-top-aligned">
            <td>
                    ${loop.index+1}
            </td>
            <td class="listContentBold">
                <zfin:link entity="${value}"/>
        </td>
        <td>
                ${value.oboID}
        </td>
        <td>
                ${value.ID}
        </td>
        <td>
                ${value.obsolete ? "- obsolete -" : ""}
                ${value.aliasesExist? "- has aliases -" : ""}
        </td>
        </tr>
    </c:if>
    </c:forEach>
</table>