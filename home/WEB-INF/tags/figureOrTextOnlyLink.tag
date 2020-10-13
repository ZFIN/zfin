<%@ tag import="org.zfin.expression.FigureType" %>
<%@ tag body-content="scriptless" %>
<%@ attribute name="figure" type="org.zfin.expression.Figure" %>
<%@ attribute name="figureStatistic" type="org.zfin.framework.presentation.FigureStatistics" %>
<%@ attribute type="java.lang.Integer" name="integerEntity" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${figureStatistic == null}">
        <c:choose>
            <c:when test="${figure.label eq 'text only'}">
                ${FigureType.TOD.name}
            </c:when>
            <c:otherwise>
              <c:choose>
                <c:when test="${figure != null && figure.label != null && !figure.publication.unpublished}">
                  ${figure.label}
                </c:when>
                <c:otherwise>
                  1 ${FigureType.FIGURE.name}
                </c:otherwise>
              </c:choose>            
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${figureStatistic.onlyTextOnlyFigures}">
               <span title="${figureStatistic.numberOfFigures}"> ${FigureType.TOD.name}</span>
            </c:when>
            <c:otherwise>
              <c:choose>
                <c:when test="${figure != null && figure.label != null && !figure.publication.unpublished}">
                  ${figure.label}
                </c:when>
                <c:otherwise>
                  1 ${FigureType.FIGURE.name}
                </c:otherwise>
              </c:choose>            
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>