<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<table class="searchresults groupstripes">
    <caption>
        Expression Pattern Search Results for <zfin:link entity="${criteria.gene}"/><br>
        <small>
            <div>
                (<zfin:choice choicePattern="0#figures|1#figure|2#figures" includeNumber="true" integerEntity="${criteria.numFound}"/>
                with expression from
                <zfin:choice choicePattern="0#publications|1#publication|2#publications" includeNumber="true" integerEntity="${criteria.pubCount}"/>)
            </div>
            <c:if test="${!criteria.onlyFiguresWithImages}">
                <div>
                    [ <a href="${criteria.linkWithImagesOnly}">Show only figures with images</a> ]
                </div>
            </c:if>
        </small>
    </caption>
    <tr>
        <th>Publication</th>
        <th>Data</th>
        <th></th>
        <th>Fish</th>
        <th>Stage Range</th>
        <th>Anatomy</th>
    </tr>
    <c:forEach items="${criteria.figureResults}" var="result" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${criteria.figureResults}" groupByBean="publication.zdbID">
            <td>
            <zfin:groupByDisplay loopName="loop" groupBeanCollection="${criteria.figureResults}" groupByBean="publication.zdbID">
                <zfin:link entity="${result.publication}"/></td>
            </zfin:groupByDisplay>
            <td>
                <zfin:link entity="${result.figure}"/>
            </td>
            <td>
                <c:if test="${!empty result.figure.images}">
                    <a href="/${result.figure.zdbID}">
                        <img border="1" height="50" src="${result.figure.img.thumbnailUrl}"/><c:if test="${fn:length(result.figure.images) > 1}">
                            <img border="0" src="/images/multibars.gif">
                        </c:if>
                    </a>
                </c:if>
            </td>
            <td><zfin:link entity="${result.fish}"/></td>
            <td></td>
            <td></td>
        </zfin:alternating-tr>
    </c:forEach>

</table>

<div style="padding: 10px 0">
    <zfin2:pagination paginationBean="${paginationBean}"/>
</div>