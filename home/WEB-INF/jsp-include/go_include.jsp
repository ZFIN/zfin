<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ page import="org.zfin.gwt.curation.ui.GoCurationModule" %>
<%@ page import="org.zfin.gwt.marker.ui.GoMarkerEditController" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--Adds the GOEditController.--%>

<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<table cellpadding="10">
    <tr align="left">
        <td colspan="1">
            <div id="${GoMarkerEditController.GO_ADD_LINK}"></div>
            <div id="${GoMarkerEditController.GO_EVIDENCE_ADD}"></div>
        </td>
    </tr>
    <tr align="center">
        <td colspan="2">
            <div id="${StandardDivNames.viewDiv}"></div>
        </td>
    </tr>
    <tr align="center">
        <td colspan="2">
            <div id="${GoCurationModule.GO_EVIDENCE_DISPLAY}"></div>
        </td>
    </tr>
    <tr align="center">
        <td colspan="2">
            <div id="${StandardDivNames.directAttributionDiv}"></div>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



