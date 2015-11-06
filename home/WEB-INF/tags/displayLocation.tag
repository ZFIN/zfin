<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" required="true" type="org.zfin.infrastructure.ZdbID" %>
<%@ attribute name="hideTitles" required="false" type="java.lang.Boolean" %>
<%@ attribute name="hideLink" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showViewMap" required="false" type="java.lang.Boolean" %>
<%@ attribute name="longDetail" required="false" type="java.lang.Boolean" %>

<c:set var="chromosome" value="${zfn:getChromosomeInfo(entity)}" scope="page"/>
<c:choose>
    <c:when test="${not empty chromosome}">
        <c:if test="${!hideTitles && chromosome ne 'Ambiguous' && !fn:contains(chromosome,'Zv9' )}">Chr: </c:if>
        ${chromosome}
        <c:if test="${!hideTitles && !hideLink}">
            <a href="/action/mapping/detail/${entity.zdbID}">
            <c:choose>
                <c:when test="${longDetail}">
                    Mapping Details/Browsers
                </c:when>
                <c:otherwise>
                    Details
                </c:otherwise>
            </c:choose>
        </a>
        </c:if>
        <c:if test="${showViewMap}">
            <zfin2:displayViewMap entity="${entity}"/>
        </c:if>
    </c:when>
    <c:otherwise>
        Unmapped
    </c:otherwise>
</c:choose>
