<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true"
        description="marker to show gbrowse image section for" %>


<%--<div class="summary">
<div class="summaryTitle">Sequence Image: </div>--%>
    <div style="float:right; border: 1px solid black;">
        <a href="/cgi-bin/gbrowse/Zv8/?name=${marker.abbreviation}">
        <img style="padding-bottom:10px; border: 0"
             src="/cgi-bin/gbrowse_img/Zv8/?grid=0&width=300&options=mRNA+0&type=mRNA&name=${marker.abbreviation}&h_feat=${marker.abbreviation}@yellow">
        </a>
    </div>

<%--
</div>--%>
