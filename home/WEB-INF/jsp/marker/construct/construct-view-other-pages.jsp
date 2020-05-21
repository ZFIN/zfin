<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:ifHasData test="${!empty formBean.otherMarkerPages}" noDataMessage="None">
    <ul class='comma-separated'>
        <c:forEach var="link" items="${formBean.otherMarkerPages}">
            <li>
                <c:if test="${!link.displayName.contains('VEGA')}">
                    <a href="${link.link}">${link.displayName}</a>
                    ${link.attributionLink}
                </c:if>
            </li>
        </c:forEach>
    </ul>
</z:ifHasData>


