<link rel="stylesheet" type="text/css" href="/Marker.css"/>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.gwt.marker.ui.StandardMarkerDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "${formBean.marker.zdbID}"
    } ;

</script>

<%--Adds the CloneEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <div id="<%=StandardMarkerDivNames.viewDiv%>"></div>
        <%--<a href="/action/antibody/detail?antibody.zdbID=${formBean.marker.zdbID}">[View Antibody]</a>--%>
    </td></tr>
    <tr>
        <td>
            <div id="<%=StandardMarkerDivNames.headerDiv%>"></div>
            <br>
            <b>Alias:</b>
            <div id="<%=StandardMarkerDivNames.previousNameDiv%>"></div>
            <br>
        </td>
        <td valign="top">
            <div id="<%=StandardMarkerDivNames.publicationLookupDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <h3>Antibody Data</h3>
            <div id="<%=StandardMarkerDivNames.dataDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td>
            <b>Antigen Genes:</b>
            <div id="<%=StandardMarkerDivNames.geneDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b>Notes:</b>
            <br>
            <div id="<%=StandardMarkerDivNames.noteDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="<%=StandardMarkerDivNames.supplierDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="<%=StandardMarkerDivNames.directAttributionDiv%>"></div>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



