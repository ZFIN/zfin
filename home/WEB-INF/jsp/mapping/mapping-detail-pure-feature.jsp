<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="marker" class="org.zfin.marker.Marker" scope="request"/>

<div class="titlebar">
    <h1>Mapping Details</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Mapping Details"/>
        </tiles:insertTemplate>
    </span>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Genomic Feature Name:</th>
        <td><span class="name-value"><zfin:link entity="${feature}"/></span></td>
    </tr>
</table>

<zfin2:subsection title="PHYSICAL MAP AND BROWSER" test="${!empty locations}" showNoData="true">
    <zfin2:PhysicalMapAndBrowserSection locations="${locations}" gbrowseImage="${gbrowseImage}"/>
</zfin2:subsection>

<div class="summary">
    <c:choose>
        <c:when test="${!otherMappingDetail}">
            <b>OTHER MAPPING INFORMATION:</b> <span class="no-data-tag">No data available</span>
        </c:when>
        <c:otherwise>
            <zfin2:otherMappingDetailFeature/>
        </c:otherwise>
    </c:choose>
</div>

<zfin2:showSingletonInfo singleLinkageList="${singletonFeatureList}"/>
