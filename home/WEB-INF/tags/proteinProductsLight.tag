<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="proteinDomainBeans" type="java.util.Collection" required="true" %>
<c:choose>
<c:when test="${fn:length(proteinDomainBeans) > 0}">
<table class="summary rowstripes">
    <caption>PROTEIN FAMILIES, DOMAINS AND SITES</caption>
    <thead>
    <tr>
        <th>Type</th>
        <th>InterPro ID</th>
        <th>Name</th>
    </tr>
    </thead>
    <c:forEach items="${proteinDomainBeans}" var="category" varStatus="loop">

        <tr class=${loop.index % 2 == 0 ? "even" : "odd"}>
            <td>${category.ipType}</td>

            <td>${category.ipID}</td>
            <td>${category.ipName}</td>

        </tr>
    </c:forEach>
</table>
</c:when>
    <c:otherwise>
        <b>PROTEIN FAMILIES, DOMAINS AND SITES</b> <span class="no-data-tag">No links to external sites available</span>
    </c:otherwise>
</c:choose>

