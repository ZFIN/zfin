<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="members" type="java.util.Collection" required="true" %>

<%--default is null--%>
<%@ attribute name="only" required="false" type="java.lang.Integer" %>

<%@ attribute name="greaterThan" required="false" type="java.lang.Integer" %>

<%--default is false --%>
<%@ attribute name="suppressTitle" required="false" type="java.lang.Boolean" %>

<%@ attribute name="columns" required="true" type="java.lang.Integer" %>

<c:set var="count" value="0"/>
<c:set var="open" value="true"/>
<table cellspacing="10px;" style="margin-left: -10px;">
    <c:forEach var="member" items="${members}" varStatus="status">
        <%--if not the PI or director--%>
        <c:choose>
            <c:when test="${!empty only}">
                <c:if test="${member.order == only}">

                    <c:if test="${open}">
                        <tr>
                        <c:set var="open" value="false"/>
                    </c:if>

                    <td>
                        <zfin2:displayMember member="${member}" suppressTitle="${suppressTitle}"/>
                    </td>
                    <c:set var="count" value="${count +1}"/>

                    <c:if test="${count % columns == 0}">
                        </tr>
                        <c:set var="open" value="true"/>
                    </c:if>
                </c:if>
            </c:when>
            <c:when test="${!empty greaterThan}">
                <c:if test="${member.order > greaterThan}">
                    <c:if test="${open}">
                        <tr>
                        <c:set var="open" value="false"/>
                    </c:if>

                    <td>
                        <zfin2:displayMember member="${member}" suppressTitle="${suppressTitle}"/>
                    </td>
                    <c:set var="count" value="${count +1}"/>

                    <%--then we close it and set it to open--%>
                    <c:if test="${count % columns == 0}">
                        </tr>
                        <c:set var="open" value="true"/>
                    </c:if>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:if test="${open}">
                    <tr>
                    <c:set var="open" value="false"/>
                </c:if>
                <td>
                    <zfin2:displayMember member="${member}" suppressTitle="${suppressTitle}"/>
                </td>
                <c:set var="count" value="${count +1}"/>

                <c:if test="${count % columns == 0}">
                    </tr>
                    <c:set var="open" value="true"/>
                </c:if>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <c:if test="${open}">
        </tr>
    </c:if>
</table>

