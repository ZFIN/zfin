<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="genotypes" type="java.util.Collection" required="false" %>
<%@ attribute name="sequenceTargetReagen" type="java.lang.String" required="false" %>

<table id="genotypes-table" class="summary rowstripes">
    <caption>GENOTYPES <c:if test="${sequenceTargetReagen != null}">CREATED WITH ${sequenceTargetReagen}</c:if></caption>
    <tr>
        <th width="20%">
            Genotype (Background)
        </th>
        <th width="20%">
            Affected Genes
        </th>
        <th width="20%">
            Phenotype
        </th>
        <th width="20%">
            Gene Expression
        </th>
    </tr>

    <c:forEach var="genotype" items="${genotypes}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>
                <zfin:link entity="${genotype.genotype}"/>
            </td>
            <td>
                <zfin:link entity="${genotype.affectedMarkers}"/>
            </td>

            <td>
                <c:if test="${genotype.numberOfFigures > 0}">
                    <c:if test="${genotype.numberOfFigures > 1}">
                        <a href='/action/genotype/genotype-phenotype-figure-summary?genoZdbID=${genotype.genotype.zdbID}'>
                            <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                         integerEntity="${genotype.numberOfFigures}"
                                         includeNumber="true"/></a>
                    </c:if>
                    <c:if test="${genotype.numberOfFigures == 1 }">
                        <a href='/${genotype.figure.zdbID}'>
                            <zfin2:figureOrTextOnlyLink figure="${genotype.figure}"
                                                        integerEntity="${genotype.numberOfFigures}"/>
                        </a>
                    </c:if>
                </c:if>

                <c:if test="${genotype.numberOfFigures == 0}">
                    &nbsp;
                </c:if>

                <c:if test="${genotype.numberOfPublications ==1}">
                    from
                    <zfin:link entity="${genotype.singlePublication}"/>
                </c:if>

                <c:if test="${genotype.numberOfPublications > 1}">
                    from
                    <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                 integerEntity="${genotype.numberOfPublications}"
                                 includeNumber="true"/>
                </c:if>

                <zfin2:showCameraIcon hasImage="${genotype.isImage}"/>

            </td>

            <td>
                <c:if test="${genotype.numberOfExpFigures > 0}">
                    <c:if test="${genotype.numberOfExpFigures > 1}">
                        <a href='/action/expression/genotype-expression-figure-summary?genoZdbID=${genotype.genotype.zdbID}&imagesOnly=false'>
                            <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                         integerEntity="${genotype.numberOfExpFigures}"
                                         includeNumber="true"/></a>
                    </c:if>

                    <c:if test="${genotype.numberOfExpFigures == 1 }">
                        <a href='/${genotype.expFigure.zdbID}'>
                            <zfin2:figureOrTextOnlyLink figure="${genotype.expFigure}"
                                                        integerEntity="${genotype.numberOfExpFigures}"/>
                        </a>
                    </c:if>
                </c:if>

                <c:if test="${genotype.numberOfExpFigures == 0}">
                    &nbsp;
                </c:if>

                <c:if test="${genotype.numberOfExpPublications ==1}">
                    from
                    <zfin:link entity="${genotype.singleExpPublication}"/>
                </c:if>

                <c:if test="${genotype.numberOfExpPublications > 1}">
                    from
                    <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                 integerEntity="${genotype.numberOfExpPublications}"
                                 includeNumber="true"/>
                </c:if>

                <zfin2:showCameraIcon hasImage="${genotype.isImageExp}"/>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
