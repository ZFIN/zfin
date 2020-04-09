<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:dataTable  hasData="${!empty formBean.otherMarkerPages}" collapse="true">
<td>
    <c:forEach var="link" items="${formBean.otherMarkerPages}" varStatus="loop">

        <c:if test="${!link.displayName.contains('VEGA')}">

            <a href="${link.link}">${link.displayName}</a>
            ${link.attributionLink}<c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:if>

    </c:forEach>
</td>
</z:dataTable>