<%@ page import="org.zfin.marker.Marker" %>
<%@ page import="org.zfin.sequence.blast.Hit" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core" %>
<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%
    Marker marker = new Marker();
    marker.setZdbID("ZDB-GENE-081507-1");
    marker.setAbbreviation("fgf8");

    Hit hit = new Hit();
    hit.setZfinAccession(marker);

    pageContext.setAttribute("hit", hit, PageContext.PAGE_SCOPE);

%>

<tagunit:assertEquals name="Create a Marker Link DB">
    <tagunit:expectedResult>
        Genes
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:markerRelationLink beanName="hit" propertyName="zfinAccession"/>
    </tagunit:actualResult>
</tagunit:assertEquals>


