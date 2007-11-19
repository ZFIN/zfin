<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="79%">

    <tr class="odd"><td colspan="3" class="sectionTitle">All active Sessions</td></tr>
    <tr>
        <td colspan="3">
            <table cellpadding="2" cellspacing="5" border="0" width="100%">
                <tr class="odd">
                    <td>ID</td>
                    <td>User Name</td>
                    <td>Me</td>
                    <td>Date Created</td>
                    <td>Time Created</td>
                    <td>Date Modified</td>
                    <td>Time Modified</td>
                    <td>Session ID</td>
                </tr>
                <c:forEach var="item" items="${formBean.activeSessions}" varStatus="rowIndex">
                    <c:choose>
                        <c:when test="${rowIndex.count % 2 != 0}">
                            <tr class="odd">
                        </c:when>
                        <c:otherwise>
                            <tr>
                        </c:otherwise>
                    </c:choose>
                    <td class="listContent">
                        <c:out value="${rowIndex.count}"/></td>
                    <td class="listContent">
                        <c:out value='${item.userName}'/>
                    </td>
                    <td class="listContent">
                        <c:if test="${formBean.request.session.id == item.sessionID}">
                            It's Me
                        </c:if>
                    </td>
                    <td class="listContent">
                        <fmt:formatDate value="${item.dateCreated}" type="Date" dateStyle="yyyy-MM-dd"/>
                    </td>
                    <td class="listContent">
                        <fmt:formatDate value="${item.dateCreated}" type="Time" timeStyle="HH:mm:ss"/>
                    </td>
                    <td class="listContent">
                        <fmt:formatDate value="${item.dateModified}" type="Date" dateStyle="yyyy-MM-dd"/>
                    </td>
                    <td class="listContent">
                        <fmt:formatDate value="${item.dateModified}" type="Time" timeStyle="HH:mm:ss"/>
                    </td>
                    <td class="listContent">
                        <c:out value='${item.sessionID}'/>
                    </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>
</table>
