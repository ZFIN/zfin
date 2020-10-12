<%@ page import="org.zfin.gwt.curation.ui.GoCurationModule" %>
<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.gwt.root.dto.OntologyDTO" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


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

<script language="javascript" src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<%--define the lookups up here--%>
<div id="<%=StandardDivNames.directAttributionDiv%>"></div>

<br>

<div id="${GoCurationModule.GO_ADD_LINK}"></div>

<br>

<div id="${GoCurationModule.GO_EVIDENCE_ADD}"></div>

<br>
<table width="100%" bgcolor="#33cc99" border="0" cellpadding="0">
    <tr><td>
    <div id="${GoCurationModule.GO_EVIDENCE_DISPLAY_FILTER}"></div>
</td></tr></table>
<br>

<div id="${GoCurationModule.GO_EVIDENCE_DISPLAY}"></div>
<%--
Add ontology name as a hidden field
--%>
<div class="GO_TERM_single" aria-hidden="true" style="display: none;"><%=OntologyDTO.GO.getOntologyName()%></div>

<%--</authz:authorize>--%>
</script>



