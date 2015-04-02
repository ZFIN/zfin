<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="marker" class="org.zfin.marker.Marker" scope="request"/>

<zfin2:dataManager zdbID="${marker.zdbID}" rtype="marker"/>
<p/>
<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar" style="">
                <span style="font-size: x-large; margin-left: 0.5em; font-weight: bold;">
                        Mapping Details
            </span>
        </td>
        <td align="right" class="titlebarRight">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Mapping Details"/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>


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

<div class="summary">
    <b>PHYSICAL MAP AND BROWSER</b>
    <c:choose>
        <c:when test="${empty locations && empty mappedClones}"><span
                class="no-data-tag">No data available</span></c:when>
        <c:otherwise>
            <zfin2:PhysicalMapAndBrowserSection marker="${marker}" locations="${locations}" gbrowseImage="${gbrowseImage}"/>
        </c:otherwise>
    </c:choose>
</div>

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

<authz:authorize ifAnyGranted="root">
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

