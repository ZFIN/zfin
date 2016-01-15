<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Fish Name: ${fish.name}
</div>
<div class="popup-body">
    <table class="primary-entity-attributes">
        <tr>
            <th>Genotype:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty fish.genotype}">
                        <zfin:link entity="${fish.genotype}"/>
                    </c:when>
                    <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
                </c:choose>

            </td>
        </tr>
        <tr>
            <th>Targeting Reagent:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty fish.strList}">
                        <zfin:link entity="${fish.strList}"/>
                    </c:when>
                    <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
                </c:choose>

            </td>
        </tr>
        </table>
    <c:if test="${!fish.genotype.wildtype}">
    <div class="summary">
        <b>GENOTYPE COMPOSITION</b>
        <c:choose>
            <c:when test="${fish.genotype.genotypeFeatures ne null && fn:length(fish.genotype.genotypeFeatures) > 0}">
                <table class="summary rowstripes">
                    <tbody>
                    <tr>
                        <th width="20%">
                            Genomic Feature
                        </th>
                        <th width="20%">
                            Construct
                        </th>
                        <th width="20%">
                            Lab of Origin
                        </th>
                        <th width="20%">
                            Zygosity
                        </th>
                        <th width="20%">
                            Parental Zygosity
                        </th>
                    </tr>
                    <c:forEach var="genoFeat" items="${fish.genotype.genotypeFeatures}" varStatus="loop">
                        <zfin:alternating-tr loopName="loop">
                            <td>
                                <zfin:link entity="${genoFeat.feature}"/>
                            </td>
                            <td>
                                <c:forEach var="construct" items="${genoFeat.feature.constructs}"
                                           varStatus="constructsloop">
                                    <a href="/action/marker/view/${construct.marker.zdbID}"><i>${construct.marker.name}</i></a><c:if
                                        test="${!constructsloop.last}">,&nbsp;</c:if>
                                </c:forEach>
                            </td>
                            <td>
                                <c:forEach var="source" items="${genoFeat.feature.sources}" varStatus="status">
                                    <c:if test="${source.organization.zdbID != 'ZDB-LAB-000914-1'}">
                                        <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${source.organization.zdbID}">
                                                ${source.organization.name}
                                        </a>
                                    </c:if>
                                    <c:if test="${!status.last}">,&nbsp;</c:if>
                                </c:forEach>
                            </td>
                            <td>
                                    ${genoFeat.zygosity.name}
                            </td>
                            <td>
                                    ${genoFeat.parentalZygosityDisplay}
                            </td>
                        </zfin:alternating-tr>
                    </c:forEach>

                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <br>No data available</br>
            </c:otherwise>
        </c:choose>
    </div>
    </c:if>
</div>