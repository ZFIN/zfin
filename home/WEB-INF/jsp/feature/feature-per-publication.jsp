<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Mutations and Transgenics:</span></th>
        <td><span class="name-value"><zfin:link entity="${publication}"/></span></td>
    </tr>
</table>

<table class="summary rowstripes">
    <tbody>
    <tr>




                    <th>Allele</th>
                    <th>Construct</th>
                    <th>Type</th>
                    <th>Affected Genomic Region(s)</th>

    </tr>
    <c:forEach var="feature" items="${featureList}"  varStatus="loop">
        <zfin:alternating-tr loopName="loop">


                           <%-- <td><zfin:link entity="${fmRel.feature}"/></td>
                            <td>
                                <zfin2:listOfTgConstructs markerCollection="${fmRel.feature.tgConstructs}"/></td>
                            <td>${fmRel.feature.type.display}</td>
                            <td>
                                <zfin2:listOfAffectedGenes markerCollection="${fmRel.feature.affectedGenes}"/>
                            </td>--%>
            <td><zfin:link entity="${feature}"/></td>
            <td>
                <zfin2:listOfTgConstructs markerCollection="${feature.tgConstructs}"/></td>
            <td>${feature.type.display}</td>
            <td>
                <zfin2:listOfAffectedGenes markerCollection="${feature.affectedGenes}"/>
            </td>


        </zfin:alternating-tr>
    </c:forEach>

    </tbody>
</table>
