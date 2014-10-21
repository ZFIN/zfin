<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="facetGroups" type="java.util.List" %>
<%@attribute name="facetQueries" type="java.util.List" %>

<div class="facet-container">
    <c:forEach var="facetGroup" items="${facetGroups}" varStatus="loop">
        <c:set var="open" value="true"/>
        <zfin2:showFacetGroup facetGroup="${facetGroup}" open="${facetGroup.open}"/>
    </c:forEach>


</div>

