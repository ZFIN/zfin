<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="relationships" required="true"
              rtexprvalue="true" type="java.util.List" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false"%>
<%@ attribute name="maxNumber" required="false" rtexprvalue="true" type="java.lang.Integer" %>

<%--set if unset--%>
<c:if test="${empty maxNumber}">
    <c:set var="maxNumber" value="1000"/>
</c:if>


<c:if test="${empty title}">
    <c:set var="title" value="MARKER RELATIONSHIPS"/>
</c:if>

<zfin2:subsection title="${title}"
                        test="${!empty relationships}" showNoData="true">

    <table class="summary horizontal-solidblock">
        <c:set var="relationshipType" value="notthesame"/>
        <c:set var="markerType" value="notthesame"/>
        <tr>
            <td>
                <zfin2:toggledProvidesLinkList collection="${relationships}" maxNumber="${maxNumber}"/>
            </td>
        </tr>
    </table>

</zfin2:subsection>

