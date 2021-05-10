<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="term" type="org.zfin.ontology.Term" required="true" %>
<%@ attribute name="additionalCssClasses" description="additional css class for the table" required="false" %>

<c:if test="${!empty term}">
<div class="ontology-term-mini-summary">
<table class="ontology-term-mini-summary <c:if test="${!empty additionalCssClasses}">${additionalCssClasses}</c:if>">
    <tr>
        <th class="name">Name:</th>
        <td class="name"><zfin:link entity="${term}" suppressPopupLink="true"/></td>
    </tr>
    <tr>
        <th class="alias">Synonyms:</th>
        <td>
            <c:forEach var="alias" varStatus="loop" items="${term.sortedAliases}">
                <span class="value mini-summary-alias-value">${alias.alias}<c:if test="${!loop.last}"><span class="alias-separator">,</span></c:if></span>
            </c:forEach>                    
        </td>
    </tr>
    <tr>
        <th class="definition">Definition:</th>
        <td>${term.definition}</td>
    </tr>
    <tr>
        <th class="ontology">Ontology:</th>
        <td>${term.ontology.commonName} [${term.oboID}] <zfin2:ontologyTermLinks term="${term}"/></td>
    </tr>
</table>
</div>
</c:if>