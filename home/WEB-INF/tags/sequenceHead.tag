<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>

<table width="100%">
    <tr bgcolor="#cccccc">
        <td colspan="2">
            <span class="name-label">SEQUENCE INFORMATION</span>
        </td>
    </tr>
</table>


<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${gene.markerType.displayName}&nbsp;Name:</span></th>
        <td><span class="name-value"><zfin:name entity="${gene}"/>  </span></td>
    </tr>
    <c:if test="${fn:startsWith(gene.markerType.name,'GENE')}">
        <tr>
            <th><span class="name-label">${gene.markerType.displayName}&nbsp;Symbol:</span></th>
            <td><span class="name-value"><zfin:link entity="${gene}"/></span></td>
        </tr>
    </c:if>
</table>



