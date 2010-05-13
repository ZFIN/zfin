<link rel="stylesheet" type="text/css" href="/Marker.css"/>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ page import="org.zfin.gwt.marker.ui.AlternateGeneEditController" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>
<%
    String zdbID = request.getParameter("zdbID");
    if(zdbID==null){
        zdbID = "AlternateZDB-GENE-001103-2" ;
    }
    String personID = request.getParameter("personID");
    if(personID==null){
        personID = "ZDB-PERS-960805-676";
    }
%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "<%= zdbID %>"
    } ;

</script>

<%--Adds the AlternateGeneEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <div id="<%=StandardDivNames.viewDiv%>"></div>
    </td></tr>
    <tr>
        <td>
            <div id="<%=StandardDivNames.headerDiv%>"></div>
            <br>
            <div id="<%=StandardDivNames.supplierDiv%>"></div>
            <br>
            <b>Previous Name(s):</b>
            <div id="<%=StandardDivNames.previousNameDiv%>"></div>
        </td>
        <td valign="top">
            <div id="<%=StandardDivNames.publicationLookupDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b>Notes:</b>
            <br>
            <div id="<%=StandardDivNames.noteDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div id="<%=StandardDivNames.geneDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <hr>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b>Sequences:</b>
            <br>
            <div id="<%=StandardDivNames.dbLinkDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="<%=StandardDivNames.directAttributionDiv%>"></div>
            <br>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



