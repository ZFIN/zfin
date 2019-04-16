<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>
<h1> Fish Tab</h1>

<div id="show-hide-all-sections"></div>

<div class="error"> Please use only for developmental purposes as this will make changes to the database!!!
</div>
<p/>

<div id="title">
    <jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>
    <b>Publication:</b> <zfin:link entity="${publication}"/> &nbsp; ${publication.zdbID}
    <br>
    <b>Title:</b> ${publication.title}<br>
</div>
<form method="GET">
    Publication ID: <label>
    <input name="publicationID" value=""/>
</label>
    &nbsp;<input type="submit" value="Submit"/>
</form>

<div id="directAttributionName"></div>

<div id="fishTab"></div>

<script type="text/javascript">
    var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "FISH_TAB",
        debug: "false"
    };
    var g = "ZDB-PUB-060105-3,ZDB-PUB-090616-53,ZDB-PUB-990507-16";
</script>

