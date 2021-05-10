<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="members" type="java.util.Collection" required="true" %>

<%--default is null--%>
<%@ attribute name="only" required="false" type="java.lang.Integer" %>

<%@ attribute name="greaterThan" required="false" type="java.lang.Integer" %>

<%--default is false --%>
<%@ attribute name="suppressTitle" required="false" type="java.lang.Boolean" %>

<%--default is false --%>
<%@ attribute name="suffix" required="false" type="java.lang.String" %>

<c:forEach var="member" items="${members}" varStatus="status">
    <%--if not the PI or director--%>
    <c:choose>
        <c:when test="${!empty only}">
            <c:if test="${member.order == only}">
                <zfin2:displayMember member="${member}" suppressTitle="${suppressTitle}"/> ${suffix}
            </c:if>
        </c:when>
        <c:when test="${!empty greaterThan}">
            <c:if test="${member.order > greaterThan}">
                <zfin2:displayMember member="${member}" suppressTitle="${suppressTitle}"/> ${suffix}
            </c:if>
        </c:when>
        <c:otherwise>
            <zfin2:displayMember member="${member}" suppressTitle="${suppressTitle}"/> ${suffix}
        </c:otherwise>
    </c:choose>
</c:forEach>

