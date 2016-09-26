<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="fishGenotypeStatistics" type="org.zfin.framework.presentation.EntityStatistics" required="true" %>
<%@ attribute name="link" type="java.lang.String" required="true" %>

<c:if test="${fishGenotypeStatistics.numberOfFigures > 0}">
    <c:if test="${fishGenotypeStatistics.numberOfFigures > 1}">
        <a href='${link}'>
            <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                         integerEntity="${fishGenotypeStatistics.numberOfFigures}"
                         includeNumber="true"/>
        </a>
    </c:if>
    <c:if test="${fishGenotypeStatistics.numberOfFigures == 1 }">
        <a href='/${fishGenotypeStatistics.figure.zdbID}'>
            <zfin2:figureOrTextOnlyLink figure="${fishGenotypeStatistics.figure}"
                                        integerEntity="${fishGenotypeStatistics.numberOfFigures}"/>
        </a>
    </c:if>
    <zfin2:showCameraIcon hasImage="${fishGenotypeStatistics.imgInFigure}"/> from
    <c:if test="${fishGenotypeStatistics.numberOfPublications ==1}">
        <zfin:link entity="${fishGenotypeStatistics.singlePublication}"/>
    </c:if>
    <c:if test="${fishGenotypeStatistics.numberOfPublications > 1}">
        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                     integerEntity="${fishGenotypeStatistics.numberOfPublications}"
                     includeNumber="true"/>
    </c:if>
</c:if>
