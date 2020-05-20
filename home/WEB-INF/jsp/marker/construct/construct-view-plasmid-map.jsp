<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>


<z:ifHasData test="${!empty formBean.marker.figures}" noDataMessage="None">
    <c:forEach var="fig" items="${formBean.marker.figures}">

        <c:forEach var="img" items="${fig.images}">
            <a href="/${img.zdbID}"><img src="/imageLoadUp/${img.medium}"></a>
        </c:forEach>
    </c:forEach>
</z:ifHasData>


