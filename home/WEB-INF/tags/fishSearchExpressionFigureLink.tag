<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figureCount" type="java.lang.Integer" required="true" %>
<%@ attribute name="queryKeyValuePair" type="java.lang.String" required="true" %>


<c:if test="${figureCount > 0}">
    <a href="/action/fish/expression-summary?${queryKeyValuePair}">
        <zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                     integerEntity="${figureCount}"/>
    </a>
</c:if>
