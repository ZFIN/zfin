<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>
<c:set var="genotype" value="${formBean.genotype}"/>

<z:dataTable collapse="true"
             hasData="${fishList != null && fn:length(fishList) > 0 }">
    <thead>
        <tr>
            <th>Fish</th>
            <th>Affected Genomic Regions</th>
            <th>Phenotype</th>
            <th>Gene Expression</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="fishGenotypeStatistics" items="${fishList}" varStatus="index">
            <zfin:alternating-tr loopName="index">
                <td><zfin:link entity="${fishGenotypeStatistics.fish}"/></td>
                <td>
                    <c:forEach var="marker" items="${fishGenotypeStatistics.affectedMarkers}"
                               varStatus="loop">
                        <zfin:link entity="${marker}"/><c:if test="${!loop.last}">, </c:if>
                    </c:forEach>
                </td>
                <td>
                    <zfin2:showFigureData
                            fishGenotypeStatistics="${fishGenotypeStatistics.fishGenotypePhenotypeStatistics}"
                            link="/action/fish/phenotype-summary?fishID=${fishGenotypeStatistics.fish.zdbID}&imagesOnly=false"/>
                </td>
                <td>
                    <zfin2:showFigureData
                            fishGenotypeStatistics="${fishGenotypeStatistics.fishGenotypeExpressionStatistics}"
                            link="/action/expression/fish-expression-figure-summary?fishID=${fishGenotypeStatistics.fish.zdbID}&imagesOnly=false"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </tbody>
</z:dataTable>