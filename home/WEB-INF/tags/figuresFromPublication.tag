<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figureData" type="org.zfin.framework.presentation.FigureData" %>
<%@ attribute name="queryKeyValuePair" type="java.lang.String" required="true" %>


<c:choose>
    <c:when test="${figureData.singleFigure}">
        <zfin:link entity="${figureData.figure}"/>
        <zfin2:showCameraIcon hasImage="${!figureData.figure.imgless}"/>
        from
        <zfin:link entity="${figureData.figure.publication}"/>
    </c:when>
    <c:otherwise>
        <zfin2:fishSearchExpressionFigureLink queryKeyValuePair="${queryKeyValuePair}"
                                              figureCount="${fn:length(figureData.figures)}"/> from
        <c:choose>
            <c:when test="${figureData.numberOfPublication ==1}">
                <zfin:link entity="${figureData.figure.publication}"/>
            </c:when>
            <c:otherwise>
                ${figureData.numberOfPublication} publications
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
