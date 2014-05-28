<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
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
        moduleType: "ENVIRONMENT_CURATION",
        debug: "false"
    }
</script>

<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<%--define the lookups up here--%>

<div id="<%=StandardDivNames.directAttributionDiv%>"></div>


<%--</authz:authorize>--%>



