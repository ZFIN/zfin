<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--Adds the GOEditController.--%>


<link rel="stylesheet" type="text/css" href="/css/zfin.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<table cellpadding="10">
    <tr align="center">
        <td colspan="2">
            <div id="<%=StandardDivNames.viewDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td>
            <%--includes qualifier, go term, pub, evidence code--%>
            <div id="<%=StandardDivNames.headerDiv%>"></div>
        </td>
        <td>
            <b>Publications:</b>
            <br>
            <div id="<%=StandardDivNames.publicationLookupDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b>Inference:</b>
            <br>
            <div id="<%=StandardDivNames.directAttributionDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b>Details:</b>
            <br>
            <div id="<%=StandardDivNames.dataDiv%>"></div>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



