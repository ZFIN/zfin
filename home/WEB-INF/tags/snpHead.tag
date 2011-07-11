<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="markerBean" type="org.zfin.marker.presentation.SnpMarkerBean"
              rtexprvalue="true" required="true" %>

<table class="primary-entity-attributes">

    <tr>
        <th><span class="name-label">${markerBean.marker.markerType.name}&nbsp;Name:</span></th>
        <td><span class="name-value"><zfin:name entity="${markerBean.marker}"/></span></td>
    </tr>

    <c:if test="${!empty markerBean.previousNames}">
        <zfin2:previousNamesFast label="Previous Names:" previousNames="${markerBean.previousNames}"/>
    </c:if>

    <tr>
        <th><span class="name-label">Variant Allele:</span></th>
        <td><span class="name-value">${markerBean.variant}</span></td>
    </tr>
    <tr>
        <th><span class="name-label">Sequence:</span></th>
        <td><zfin2:snpSequence markerBean="${markerBean}"/></td>
    </tr>

    <zfin2:notesInDiv hasNotes="${formBean.marker}"/>


</table>


