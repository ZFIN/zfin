<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="location" required="true" type="org.zfin.mapping.GenomeLocation" %>
<%@ attribute name="marker" required="true" type="org.zfin.marker.Marker" %>

<c:if test="${empty locations}">
    <c:set var="location" value="${zfn:getGenomeLocation(marker)}"/>
</c:if>

<c:set var="tracks" value="${zfn:getGbrowseTrackUrlAddition(marker)}"/>
<c:set var="gbrowseCoordinates" value="${location.chromosome}:${location.centeredStart}..${location.centeredEnd}"/>

<div style="margin: .0em; border: 1px solid black ; background: white; width: 800px; display: none" id="gBrowseImage">
    <c:choose>
        <c:when test="${isClone}">
            <a href="http://zfin.org/gb2/gbrowse/zfin_ensembl/?name=genomic_clone:${marker.zdbID}">
                <img style="padding-bottom:10px; border: 0 " onload="showDiv();"
                     src="http://zfin.org/gb2/gbrowse_img/zfin_ensembl/?grid=0&options=genes&name=genomic_clone:${marker.zdbID}&width=700${tracks}&h_feat=${marker.abbreviation}">
            </a>
        </c:when>
        <c:otherwise>
            <a href="http://zfin.org/gb2/gbrowse/zfin_ensembl/?name=${gbrowseCoordinates}">
                <img style="padding-bottom:10px; border: 0 " onload="showDiv();"
                     src="http://zfin.org/gb2/gbrowse_img/zfin_ensembl/?grid=0&options=genes&name=${gbrowseCoordinates}&h_feat=${marker.abbreviation}@pink&width=700${tracks}"
                     id="test">
            </a>
        </c:otherwise>
    </c:choose>
</div>

<script>

    function showDiv() {
        jQuery('#gBrowseImage').show();
    }
</script>