<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="summary rowstripes">
    <caption>OTHER MAPPING INFORMATION</caption>
</table>

<zfin2:showSingletonInfo singleLinkageList="${singletonList}"/>

<c:if test="${markersEncodedByMarkers.size() > 0}">

</c:if>


<c:if test="${markersEncodedByMarkers.size() > 0}">
    <table class="summary rowstripes">
    <tr>
        <th>
            Markers Encoded by <zfin:abbrev entity="${marker}"/>
        </th>
    </tr>
    <c:forEach var="marker" items="${markersEncodedByMarkers}">
        <tr>
            <td colspan="3"><zfin:link entity="${marker}"/>
                <zfin2:displayLocation entity="${marker}" showViewMap="true"/>
            </td>
        </tr>
    </c:forEach>
</c:if>
<c:if test="${markersContainedIn.size() > 0}">
    <table class="summary rowstripes">
        <tr>
            <th colspan="3">
                Markers Contained in <zfin:abbrev entity="${marker}"/>
            </th>
        </tr>
        <c:forEach var="marker" items="${markersContainedIn}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td width="5%"><zfin:link entity="${marker}"/></td>
                <td width="15%"><zfin2:displayLocation entity="${marker}" hideLink="true"/></td>
                <td width="80%">
                    <a href="/action/mapping/detail/${marker.zdbID}">Details</a>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>
<c:if test="${estContainingSNP.size() > 0}">
    <table class="summary rowstripes">
        <tr>
            <th>
                EST Containing <zfin:abbrev entity="${marker}"/>
            </th>
        </tr>
        <c:forEach var="marker" items="${estContainingSNP}">
            <tr>
                <td colspan="3">
                    <zfin:link entity="${marker}"/>
                    <zfin2:displayLocation entity="${marker}" showViewMap="true"/>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>
<c:if test="${geneContainingSNP.size() > 0}">
    <table class="summary rowstripes">
        <tr>
            <th>
                Gene Containing <zfin:abbrev entity="${marker}"/>
            </th>
        </tr>
        <c:forEach var="marker" items="${geneContainingSNP}">
            <tr>
                <td colspan="3">
                    <zfin:link entity="${marker}"/>
                    <zfin2:displayLocation entity="${marker}"/>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>
<c:if test="${not empty mappedFeatureMarkers}">
    <c:forEach var="mappedFeature" items="${mappedFeatureMarkers}">
        <table class="summary rowstripes">
        <tr>
            <th>
                Genomic Feature <zfin:link entity="${mappedFeature[0].feature}"/> is an allele of
                <zfin:link entity="${marker}"/>
            </th>
        </tr>
        <tr>
            <td colspan="3">
                <zfin2:displayGeneticMapping mappedMarkers="${mappedFeature}" hideTitle="true"
                                             targetEntity="${mappedFeature[0].feature}"/>
            </td>
        </tr>
    </c:forEach>
</c:if>

<c:if test="${not empty linkageFeatureList}">
    <c:forEach var="linkagePerFeatureList" items="${linkageFeatureList}">
        <table class="summary rowstripes">
            <tr>
                <th>
                    Genomic Feature
                    <zfin:link entity="${linkagePerFeatureList[0].feature}"/> is an allele of
                    <zfin:link entity="${marker}"/>
                </th>
            </tr>
            <tr>
                <td colspan="3">
                    <zfin2:displayMappingFromPublications linkageMemberList="${linkagePerFeatureList}"
                                                          hideTitle="true"/>
                </td>
            </tr>
        </table>
    </c:forEach>
</c:if>

<zfin2:otherMappingDetailFeature/>

<zfin2:showSingletonInfoWithFeature singleLinkageMap="${singletonFeatureMapList}"/>

<c:if test="${primerSetList.size() >=1}">
    <table class="summary rowstripes">
        <caption>Primer Sets:</caption>
        <tr>
            <th style="width: 10%">Strain</th>
            <th style="width: 10%">Bandsize</th>
            <th style="width: 20%">Restriction Enzyme</th>
            <th style="width: 20%">Annealing Temperature [C]</th>
        </tr>
        <c:forEach var="primerSet" items="${primerSetList}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
        <td><zfin:link entity="${primerSet.genotype}"/></td>
        <td>${primerSet.bandSize}</td>
        <td>${primerSet.restrictionEnzyme}</td>
        <td>${primerSet.annealingTemperature}</td>
        </tr>
        <tr>
            <td>Forward Primer</td>
            <td>${primerSet.forwardPrimer}</td>
        </tr>
        <tr>
            <td>Reverse Primer</td>
            <td>${primerSet.reversePrimer}</td>
            </zfin:alternating-tr>
            </c:forEach>
    </table>
</c:if>

</table>
