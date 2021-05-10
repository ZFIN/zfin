<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:choose>
    <c:when test="${fromBean.numberOfConstructs < 10}">
        <c:forEach var="hyperlinkEntity" items="${formBean.constructs}" varStatus="loop">
            <i></i><zfin:link entity="${hyperlinkEntity}" suppressPopupLink="${suppressPopupLinks}"/></i><c:if
                test="${showAttributionLinks}"> <zfin:attribution
                entity="${hyperlinkEntity.superterm}"/></c:if><c:if test="${!loop.last}">, </c:if>
        </c:forEach>&nbsp;
    </c:when>
    <c:otherwise>
        <table>
            <c:forEach var="hyperlinkEntity" items="${formBean.constructs}" varStatus="loop">
                <tr>
                    <td>
                        <zfin:link entity="${hyperlinkEntity}" suppressPopupLink="${suppressPopupLinks}"/><c:if
                            test="${showAttributionLinks}"> <zfin:attribution
                            entity="${hyperlinkEntity.superterm}"/></c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </c:otherwise>
</c:choose>
