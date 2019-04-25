<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script type="text/javascript" language="javascript"
        src="/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>
<h1> Human Disease Curation </h1>

<div id="show-hide-all-sections"></div>

<div class="error"> Please use only for developmental purposes as this will make changes to the database!!!
</div>
<p/>

<div id="title">
    <jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>
    Publication: <zfin:link entity="${publication}"/> &nbsp; ${publication.zdbID}
    <br>
    Title: ${publication.title}<br>
</div>
<form method="GET">
    Publication ID: <label>
    <input name="publicationID" value=""/>
</label>
    &nbsp;<input type="submit" value="Submit"/>
</form>

<div id="<%=StandardDivNames.directAttributionDiv%>"></div>

<div id="humanDiseaseZone"></div>

<script type="text/javascript">
    var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "DISEASE_CURATION",
        debug: "false"
    };
</script>

