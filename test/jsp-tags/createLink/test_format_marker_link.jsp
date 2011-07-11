<%@ page import="org.zfin.marker.Marker" %>
<%@ page import="org.zfin.marker.MarkerType" %>
<%@ page import="org.zfin.sequence.reno.Candidate" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    Marker marker = new Marker();
    marker.setZdbID("ZDB-GENE-081507-1");
    marker.setAbbreviation("fgf8");
    marker.setName("fibroblast growth factor 8 a");
    MarkerType type = new MarkerType();
    type.setType(Marker.Type.GENE);
    Set<Marker.TypeGroup> groups = new HashSet<Marker.TypeGroup>();
    groups.add(Marker.TypeGroup.GENEDOM);
    type.setTypeGroups(groups);
    marker.setMarkerType(type);
    Candidate candidate = new Candidate();
    candidate.setIdentifiedMarker(marker);

    pageContext.setAttribute("formBean", candidate, PageContext.REQUEST_SCOPE);

%>

<zfin:link entity="${formBean.identifiedMarker}"/>
<tagunit:assertEquals name="Create a Marker Link">
    <tagunit:expectedResult>
        <a href="/action/marker/view/ZDB-GENE-081507-1"><span class="genedom" title="fibroblastgrowthfactor8a">fgf8</span></a>
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:link entity="${formBean.identifiedMarker}"/>
    </tagunit:actualResult>
</tagunit:assertEquals>


<tagunit:assertEquals name="Create a Marker Name Span tag">
    <tagunit:expectedResult>
        <span class="genedom"title="fgf8">fibroblastgrowthfactor8a</span>
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:name entity="${formBean.identifiedMarker}"/>
    </tagunit:actualResult>
</tagunit:assertEquals>



<tagunit:assertEquals name="Create a Marker Abbreviation Span tag">
    <tagunit:expectedResult>
        <span class="genedom"title="fibroblastgrowthfactor8a">fgf8</span>
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:abbrev entity="${formBean.identifiedMarker}"/>
    </tagunit:actualResult>
</tagunit:assertEquals>


