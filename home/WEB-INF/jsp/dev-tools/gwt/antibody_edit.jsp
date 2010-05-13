<link rel="stylesheet" type="text/css" href="/Marker.css"/>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>
<%
    String zdbID = request.getParameter("zdbID");
    if(zdbID==null){
        zdbID = "ZDB-ATB-081002-19" ;
    }

    String antibodyDefPubZdbID = request.getParameter("antibodyDefPubZdbID");
//    if(antibodyDefPubZdbID==null){
//        antibodyDefPubZdbID = "" ;
//    }
    String personID = request.getParameter("personID");
    if(personID==null){
        personID = "ZDB-PERS-960805-676";
    }
%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "<%= zdbID %>",
        antibodyDefPubZdbID: "<%=antibodyDefPubZdbID%>"
    } ;
</script>

<%--Adds the CloneEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <div id="<%=StandardDivNames.viewDiv%>"></div>
        <%--<a href="/action/antibody/detail?antibody.zdbID=${formBean.marker.zdbID}">[View Antibody]</a>--%>
    </td></tr>
    <tr>
        <td>
            <div id="<%=StandardDivNames.headerDiv%>"></div>
            <br>
            <b>Alias:</b>
            <div id="<%=StandardDivNames.previousNameDiv%>"></div>
            <br>
        </td>
        <td valign="top">
            <div id="<%=StandardDivNames.publicationLookupDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <h3>Antibody Data</h3>
            <div id="<%=StandardDivNames.dataDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td>
            <b>Antigen Genes:</b>
            <div id="<%=StandardDivNames.geneDiv%>"></div>
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
        <td colspan="2">
            <b>Suppliers:</b>
            <br>
            <div id="<%=StandardDivNames.supplierDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="<%=StandardDivNames.directAttributionDiv%>"></div>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



