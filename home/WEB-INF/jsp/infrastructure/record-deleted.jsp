<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <authz:authorize access="hasRole('root')">
        <c:choose>
            <c:when test="${!empty formBean.errors}">
                <ul>
                    <c:forEach var="error" items="${formBean.errors}">
                        <li><span class="error">${error}</span></li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <div class="caution-text">${formBean.recordToDeleteViewString} has been deleted! </div>
                <c:if test="${formBean.removedFromTracking}">
                    <div class="caution-text">This feature has also been deleted from feature tracking. </div>
                </c:if>
                <c:if test="${formBean.publicationCurated != null}">
                    <p/><div class="caution-text">Go back to <a href='/action/curation/${formBean.publicationCurated.zdbID}'>${formBean.publicationCurated.shortAuthorList}</a>. </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </authz:authorize>
</z:page>