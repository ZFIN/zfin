<%@ tag import="org.zfin.anatomy.presentation.AnatomyLabel" %>
<%@ tag import="org.zfin.expression.Figure" %>
<%@ tag body-content="scriptless" %>
<%@ attribute name="figure" type="org.zfin.expression.Figure" %>
<%@ attribute name="figureStatistic" type="org.zfin.framework.presentation.FigureStatistics" %>
<%@ attribute type="java.lang.Integer" name="integerEntity" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${figureStatistic == null}">
        <c:choose>
            <c:when test="${figure.label eq 'text only'}">
                <%= Figure.Type.TOD.getName()%>
            </c:when>
            <c:otherwise>
                1  <%= Figure.Type.FIGURE.getName()%>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${figureStatistic.onlyTextOnlyFigures}">
               <span title="${figureStatistic.numberOfFigures}"> <%= Figure.Type.TOD.getName()%></span>
            </c:when>
            <c:otherwise>
                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                             integerEntity="${integerEntity}"
                             includeNumber="true"/>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>