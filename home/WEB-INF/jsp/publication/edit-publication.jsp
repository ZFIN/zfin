<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<c:set var="linkURL">/cgi-bin/webdriver?MIval=aa-link_authors.apg&OID=${publication.zdbID}&anon1=zdb_id&anon1text=${publication.zdbID}</c:set>
<c:set var="trackURL">/action/publication/${publication.zdbID}/track</c:set>
<c:if test="${allowCuration}">
    <c:set var="curateURL">/cgi-bin/webdriver?MIval=aa-curation.apg&OID=${publication.zdbID}</c:set>
</c:if>


<div class="container-fluid">
    <zfin2:dataManager zdbID="${publication.zdbID}"
                       linkURL="${linkURL}"
                       trackURL="${trackURL}"
                       curateURL="${curateURL}"
                       rtype="publication"/>

    <div class="row">
        <div class="col-xs-12">
            <h2>Editing ${publication.zdbID}</h2>
        </div>
    </div>

    <zfin2:publicationForm publication="${publication}" error="${error}"/>
</div>
