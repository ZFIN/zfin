<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<z:dataTable hasData="${!empty formBean.fish}" collapse="true">
    <thead>
    <tr>
        <th>Fish</th>
        <th>Affected Genomic Regions</th>
        <th>Phenotype</th>
        <th>Gene Expression</th>
    </tr>
    </thead>
    <c:forEach var="fishGenotypeStatistics" items="${formBean.fish}" varStatus="index">
        <tr>
            <td>
                <zfin:link entity="${fishGenotypeStatistics.fish}"/>
            </td>
            <td>
                <ul class="comma-separated">
                    <c:forEach var="marker" items="${fishGenotypeStatistics.affectedMarkers}">
                    <li><zfin:link entity="${marker}"/></li>
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
        </tr>
    </c:forEach>
    </table>
</z:dataTable>
