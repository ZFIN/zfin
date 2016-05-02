<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker"
              rtexprvalue="true" required="true" %>
<%@ attribute name="typeName" type="java.lang.String" required="false" rtexprvalue="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>

<c:if test="${empty typeName}">
    <c:set var="typeName">${marker.markerType.displayName}</c:set>
</c:if>

<table class="primary-entity-attributes">

    <tr>
      <th><span class="name-label">${typeName}&nbsp;Name:</span></th>
      <td><span class="name-value"><zfin:name entity="${marker}"/></span></td>
    </tr>

    <c:if test="${!empty previousNames}">
        <c:choose>

        <c:when test="${formBean.marker.type eq 'GENE'}">
              <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}"/>
        </c:when>
        <c:otherwise>
              <zfin2:previousNamesFast  previousNames="${previousNames}"/>
        </c:otherwise>
        </c:choose>
    </c:if>
    <c:if test="${formBean.marker.type ne 'EFG'&& formBean.marker.type ne 'REGION'&& !(fn:contains(formBean.marker.type,'CONSTRCT'))}">
        <%--<c:if test="${formBean.marker.type ne 'REGION'}">--%>
    <tr>
        <th>Location:</th>
        <td>
            <zfin2:displayLocation entity="${formBean.marker}"/>
        </td>
    </tr>
</c:if>
    <%--</c:if>--%>
    <zfin2:entityNotes entity="${formBean.marker}"/>

</table>


