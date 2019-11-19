<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("jquery-ui.css")}">
<script src="${zfn:getAssetPath("jquery-ui.js")}"></script>


<div class="mb-3">
    <span class="bold">CREATE NEW CONSTRUCT: </span>
    <a onclick="showCreateConstructSection()" id="showConstructAdd" style="text-decoration:underline">Show</a>
    <a style="display: none;" onclick="hideCreateConstructSection()" id="hideConstructAdd">Hide</a>

    <div id="constructAddContainer" style="display: none;"></div>
</div>

<div class="mb-3">
    <span class="bold">EDIT CONSTRUCT: </span>
    <a onclick="showEditConstructSection()" id="showConstructEdit" style="text-decoration:underline">Show</a>
    <a style="display: none;" onclick="hideEditConstructSection()" id="hideConstructEdit">Hide</a>

    <div id="constructEditContainer" style="display: none;"></div>
</div>

<div class="mb-3">
    <div id="construct-relationship-link"></div>
    <div id="construct-relationship"></div>
</div>


<script type="text/javascript">
    jQuery('#constructAddContainer').load('/action/construct/construct-add?constructPublicationZdbID=${publication.zdbID}');
    jQuery('#constructEditContainer').load('/action/construct/construct-update?constructPublicationZdbID=${publication.zdbID}');
    function showCreateConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featcreation', 'show', '/action/curation/${publication.zdbID}');
        jQuery('#constructAddContainer').show();
        jQuery('#showConstructAdd').hide();
        jQuery('#hideConstructAdd').show();
    }
    function hideCreateConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featcreation', 'hide', '/action/curation/${publication.zdbID}');
        jQuery('#constructAddContainer').hide();
        jQuery('#showConstructAdd').show();
        jQuery('#hideConstructAdd').hide();
    }
    function showEditConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featediting', 'show', '/action/curation/${publication.zdbID}');
        jQuery('#constructEditContainer').show();
        jQuery('#showConstructEdit').hide();
        jQuery('#hideConstructEdit').show();
    }
    function hideEditConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featediting', 'hide', '/action/curation/${publication.zdbID}');
        jQuery('#constructEditContainer').hide();
        jQuery('#showConstructEdit').show();
        jQuery('#hideConstructEdit').hide();
    }
</script>