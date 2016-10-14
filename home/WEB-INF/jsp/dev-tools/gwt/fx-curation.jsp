<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css"/>
<script type="text/javascript" language="javascript" src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>
<h1> Expression (FX) Curation Application</h1>

<div id="show-hide-all-sections"></div>

<div class="error"> Please use only for developmental purposes as this will make changes to the database!!!
</div>
<p/>

<div title="Hello">
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


<div id="expressionZone"></div>

<script type="text/javascript">
    var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "EXPRESSION_CURATION",
        debug: "false"
    };
    var g = "ZDB-PUB-060105-3,ZDB-PUB-090616-53,ZDB-PUB-990507-16,ZDB-PUB-070210-20";
</script>

