
<%@ page import="org.zfin.gwt.curation.ui.FeatureCurationModule" %>
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
        moduleType: "FEATURE_CURATION",
        debug: "false"
    }
</script>

<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<%--define the lookups up here--%>
<div id="<%=StandardDivNames.directAttributionDiv%>"></div>

<br>
<div id="<%=FeatureCurationModule.FEATURE_ADD_LINK%>"></div>
<div id="<%=FeatureCurationModule.FEATURE_ADD%>"></div>
<br>
<div id="<%=FeatureCurationModule.FEATURE_EDIT_LINK%>"></div>
<div id="<%=FeatureCurationModule.FEATURE_EDIT%>"></div>

<br>
<div id="<%=FeatureCurationModule.FEATURE_RELATIONSHIP_LINK%>"></div>
<div id="<%=FeatureCurationModule.FEATURE_RELATIONSHIP%>"></div>

<br>
<br>



<%--</authz:authorize>--%>



