<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ page import="org.zfin.framework.presentation.CurationTestController" %>
<%@ page import="org.zfin.gwt.curation.ui.GoCurationModule" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--Adds the GOEditController.--%>
<%
    String pubID = request.getParameter("zdbID");
    if(pubID ==null){
        pubID = "ZDB-PUB-080701-3" ;
    }
%>

<script type="text/javascript">
    var curationProperties = {
        zdbID : "<%=pubID%>",
        moduleType: "GO_CURATION",
        debug: "false"
    }
</script>

<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<%--define the lookups up here--%>
<div id="<%=StandardDivNames.directAttributionDiv%>"></div>

<br>

<div id="<%=GoCurationModule.GO_ADD_LINK%>"></div>

<br>

<div id="<%=GoCurationModule.GO_EVIDENCE_ADD%>"></div>

<br>
<table width="100%" bgcolor="#33cc99" border="0" cellpadding="0">
    <tr><td>
    <div id="<%=GoCurationModule.GO_EVIDENCE_DISPLAY_FILTER%>"></div>
</td></tr></table>
<br>

<div id="<%=GoCurationModule.GO_EVIDENCE_DISPLAY%>"></div>

<%--</authz:authorize>--%>



