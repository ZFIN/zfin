<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize access="hasRole('root')">--%>
<c:set var="zdbID" value="${param.zdbID}" />
<c:if test="${empty zdbID}">
    <c:set var="zdbID" value="ZDB-GENE-001103-2" />
</c:if>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "${zdbID}"
    } ;

</script>

<%--Adds the CloneEditController.--%>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<div id="newProteinSequence"></div>
<br>
<br>
<div id="newStemLoopSequence"></div>

<%--</authz:authorize>--%>



