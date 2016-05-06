<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="marker" class="org.zfin.marker.Marker" scope="request"/>

<zfin2:dataManager zdbID="${marker.zdbID}" rtype="marker"/>
<p/>

<div class="titlebar">
    <h1>Mapping Details</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Mapping Details"/>
        </tiles:insertTemplate>
    </span>
</div>

<table class="primary-entity-attributes">
    <c:choose>
        <c:when test="${marker.type.name() eq 'GENE'}">
            <tr>
                <th><span class="name-label">Gene Name:</span></th>
                <td><span class="name-value"><zfin:name entity="${marker}"/></span></td>
            </tr>
            <tr>
                <th><span class="name-label">Symbol:</span></th>
                <td><span class="name-value"><zfin:link entity="${marker}"/></span></td>
            </tr>
        </c:when>
        <c:otherwise>
            <tr>
                <th><span class="name-label">${marker.type}:</span></th>
                <td><span class="name-value"><zfin:link entity="${marker}"/></span></td>
            </tr>
        </c:otherwise>
    </c:choose>
</table>

<zfin2:subsection title="PHYSICAL MAP AND BROWSER" test="${!empty locations || !empty mappedClones}" showNoData="true">
    <c:if test="${!isClone}">
        <zfin2:PhysicalMapAndBrowserSection locations="${locations}" gbrowseImage="${gbrowseImage}"/>
    </c:if>
    <zfin2:mappedClonesTable mappedClones="${mappedClones}" marker="${marker}"/>
</zfin2:subsection>

<zfin2:subsection title="PHYSICAL MAPPING " test="${!empty allelicFeatures}" showNoData="true">

    <zfin2:displayFeatureLocations allelicFeatures="${allelicFeatures}"/>
</zfin2:subsection>
<p/>

<div class="summary">
    <c:choose>
        <c:when test="${mappedMarkers.size() == 0}">
            <b>GENETIC MAPPING PANELS:</b> <span class="no-data-tag">No data available</span>
        </c:when>
        <c:otherwise>
            <zfin2:displayGeneticMapping mappedMarkers="${mappedMarkers}" targetEntity="${marker}"/>
        </c:otherwise>
    </c:choose>
</div>

<zfin2:displayMappingFromPublications linkageMemberList="${linkageMemberList}"/>

<p/>

<div class="summary">
    <c:choose>
        <c:when test="${!otherMappingDetail}">
            <b>OTHER MAPPING INFORMATION:</b> <span class="no-data-tag">No data available</span>
        </c:when>
        <c:otherwise>
            <zfin2:otherMappingDetail/>
        </c:otherwise>
    </c:choose>
</div>

<c:if test="${not empty feature}">
    <table class="primary-entity-attributes">
        <tr>
            <td><span class="name-value">Genomic Feature <zfin:link entity="${feature}"/>
        is an allele of <zfin:link entity="${marker}"/>"
        </span></td>
        </tr>
    </table>
</c:if>

<authz:authorize access="hasRole('root')">
    <p/>

    <div id="toggleMartOn"><a href="javascript:toggleOn('mart','toggleMartOff','toggleMartOn')">Show Chromosome Mart
        (invisible to public)</a></div>
    <div style="display: none" id="toggleMartOff"><a href="javascript:toggleOf('mart','toggleMartOff','toggleMartOn')">Hide
        Chromosome
        Mart</a></div>
    <div style="display: none" id="mart">
        <table class="summary rowstripes">
            <caption>Chromosome Mart:</caption>
            <tr>
                <th style="width: 10%">Chr</th>
                <th>Accession</th>
                <th style="width: 10%">Source</th>
                <th>Subsource</th>
                <th>Start</th>
                <th>Stop</th>
            </tr>
            <c:forEach var="location" items="${markerGenomeLocations}" varStatus="loop">
                <tr>
                    <td>${location.chromosome}</td>
                    <td>${location.accessionNumber}</td>
                    <td>${location.source}</td>
                    <td>${location.detailedSource}</td>
                    <td>${location.start}</td>
                    <td>${location.end}</td>
                </tr>
            </c:forEach>
        </table>
    </div>
    <script type="text/javascript">
        function toggleOn(showElement1, showElement2, hideElement) {
            jQuery('#' + showElement1).show();
            jQuery('#' + showElement2).show();
            jQuery('#' + hideElement).hide();
        }
        function toggleOf(showElement1, showElement2, hideElement) {
            jQuery('#' + showElement1).hide();
            jQuery('#' + showElement2).hide();
            jQuery('#' + hideElement).show();
        }
    </script>

</authz:authorize>

