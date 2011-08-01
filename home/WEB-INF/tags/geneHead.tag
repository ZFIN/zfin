<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="false" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${gene.markerType.displayName} Name:</span></th>
        <td><span class="name-value"><zfin:name entity="${gene}"/></span></td>
    </tr>
    <tr>
        <th><span class="name-label">${gene.markerType.displayName} Symbol:</span></th>
        <td><span class="name-value"><zfin:abbrev entity="${gene}"/></span></td>
    </tr>

    <c:if test="${!empty previousNames}">
        <zfin2:previousNamesFast label="Previous Names:" previousNames="${previousNames}"/>
    </c:if>
    <c:if test="${formBean.hasMarkerHistory}">
        <tr>
            <td colspan="2">
                <a class="data-note" href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()
    %>?MIval=aa-nomenview.apg&OID=${formBean.marker.zdbID}&abbrev=${formBean.marker.abbreviation}">Nomenclature
                    History</a>
            </td>
        </tr>
    </c:if>
    <zfin2:notesInDiv hasNotes="${gene}"/>

</table>




