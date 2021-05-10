<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.gwt.marker.ui.GoMarkerEditController" %>
<%--<authz:authorize access="hasRole('root')">--%>
<c:set var="zdbID" value="${param.get(GoMarkerEditController.LOOKUP_ZDBID)}" />
<c:if test="${empty zdbID}">
    <c:set var="zdbID" value="ZDB-GENE-011026-1" />
</c:if>
<c:set var="pubID" value="${param.get(GoMarkerEditController.PUB_ZDBID)}" />
<c:set var="markerID" value="${param.get(GoMarkerEditController.GENE_ZDBID)}" />

<z:devtoolsPage title="GWT GO Edit">
    <script type="text/javascript">
        var MarkerProperties= {
            ${GoMarkerEditController.STATE_STRING}: "${GoMarkerEditController.GO_EVIDENCE_DISPLAY}",
        ${GoMarkerEditController.LOOKUP_ZDBID} : "${zdbID}",
        ${GoMarkerEditController.GENE_ZDBID}: "${markerID}",
        ${GoMarkerEditController.PUB_ZDBID} : "${pubID}"
        }

    </script>


    <jsp:include page="../../../jsp-include/go_include.jsp"/>
</z:devtoolsPage>
