<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="facetGroup" type="org.zfin.search.presentation.FacetGroup" required="true" %>
<%@attribute name="open" type="java.lang.Boolean" required="true" %>

<c:choose>
    <c:when test="${fn:length(facetGroup.facets) > 1}"><c:set var="showFacetLabel" value="true"/></c:when>
    <c:otherwise><c:set var="showFacetLabel" value="false"/></c:otherwise>
</c:choose>

<%-- to make a DOM class/id, just strip spaces, and maybe slashes... there aren't any yet, but we can't seem
     to help ourselves when it comes to naming with slashes --%>
<c:set var="name" value="${zfn:makeDomIdentifier(facetGroup.label)}" />

<%-- this is a little awkard, but the goal is that it should be hidden for non-root users if facetGroup.rootOnly is true --%>
<c:set var="hidden" value="false"/>
<c:set var="rootOnlyCssClass" value=""/>

<c:if test="${facetGroup.rootOnly}">
    <c:set var="hidden" value="true"/>
    <c:set var="rootOnlyCssClass" value="root-only-facet-group"/>
</c:if>

<authz:authorize access="hasRole('root')">
    <c:set var="hidden" value="false"/>
</authz:authorize>

<c:if test="${!hidden}">
    <div class="facet-group ${rootOnlyCssClass}" data-name="${category}:${name}">
        <div id="${name}-facet-group-label-container" class="facet-group-label-container">
            <i class="icon-toggle fa fa-fw fa-chevron-right <c:if test="${open}">open</c:if>"></i>
            ${facetGroup.label}
        </div>
        <div id="${name}-facet-group-values" class="facet-group-values <c:if test="${facetGroup.label == 'Category'}">category-facet-group-values</c:if>"
                <c:if test="${!open}"> style="display:none;"</c:if>>
            <ol class="facet-field-list list-unstyled">
                <c:forEach var="facetQuery" items="${facetGroup.facetQueries}" varStatus="loop">
                    <li><zfin-search:showFacetQuery open="true" gaCategory="${zfn:buildFacetedSearchGACategory(category, facetGroup.label)}" facetQuery="${facetQuery}"/></li>
                </c:forEach>
                <c:forEach var="facet" items="${facetGroup.facets}" varStatus="loop">
                    <li><zfin2:showFacetField showLabel="${showFacetLabel}" facet="${facet}" open="${facet.open}"/></li>
                </c:forEach>
            </ol>
        </div>
    </div>
</c:if>


