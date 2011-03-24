<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="terms" scope="request" type="java.util.Set"/>
<jsp:useBean id="ontology" scope="request" type="org.zfin.ontology.Ontology"/>
<jsp:useBean id="query" scope="request" type="java.lang.String"/>

<h2>Ontology Search <c:if test="${ontology != null}">${ontology.commonName}</c:if></h2>

Query String: ${query}
<p></p>
Total of: ${fn:length(terms)}

<table width="90%">
    <tr class="search-result-table-header left-top-aligned">
        <c:if test="${ontology == null}">
            <td class="sectionTitle">Ontology</td>
        </c:if>
        <td class="sectionTitle">Term Name</td>
        <td class="sectionTitle">Synonyms</td>
    </tr>

    <c:forEach var="matchingTerm" items="${terms}" varStatus="loop">
        <tr class="search-result-table-entries left-top-aligned">
            <c:if test="${ontology == null}">
                <td>
                        ${matchingTerm.term.ontology.commonName}
                </td>
            </c:if>
            <td>
                <zfin:link entity="${matchingTerm.term}"/>
            </td>
            <td>
                <ul>
                    <c:forEach var="alias" items="${matchingTerm.term.aliases}">
                        <li><zfin:highlight highlightEntity="${alias}" highlightString="${query}"/></li>
                    </c:forEach>
                </ul>
            </td>
        </tr>
    </c:forEach>
</table>

