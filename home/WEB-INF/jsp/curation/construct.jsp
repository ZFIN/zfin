<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<script type="text/javascript">
    var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "CONSTRUCT_CURATION",
        debug: "false"
    }
</script>

<p></p>
<table class="table table-borderless">
    <tr>
        <td>
            <span class="bold">CREATE NEW CONSTRUCT: </span>
            <a onclick="showCreateConstructSection()" id="showConstructAdd" style="text-decoration:underline">Show</a>
            <a style="display: none;" onclick="hideCreateConstructSection()" id="hideConstructAdd">Hide</a>

            <div id="constructadd" style="display: none;"></div>
        </td>
    </tr>
    <tr>
        <td>
            <span class="bold">EDIT CONSTRUCT: </span>
            <a onclick="showEditConstructSection()" id="showConstructEdit" style="text-decoration:underline">Show</a>
            <a style="display: none;" onclick="hideEditConstructSection()" id="hideConstructEdit">Hide</a>

            <div id="constructedit" style="display: none;"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div id="construct-relationship-link"></div>
            <div id="construct-relationship"></div>
        </td>
    </tr>
</table>

<script type="text/javascript">
    jQuery('#constructadd').load('/action/construct/construct-add?constructPublicationZdbID=${publication.zdbID}');
    jQuery('#constructedit').load('/action/construct/construct-update?constructPublicationZdbID=${publication.zdbID}');
    function showCreateConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featcreation', 'show', '/action/curation/${publication.zdbID}');
        jQuery('#constructadd').show();
        jQuery('#showConstructAdd').hide();
        jQuery('#hideConstructAdd').show();
    }
    function hideCreateConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featcreation', 'hide', '/action/curation/${publication.zdbID}');
        jQuery('#constructadd').hide();
        jQuery('#showConstructAdd').show();
        jQuery('#hideConstructAdd').hide();
    }
    function showEditConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featediting', 'show', '/action/curation/${publication.zdbID}');
        jQuery('#constructedit').show();
        jQuery('#showConstructEdit').hide();
        jQuery('#hideConstructEdit').show();
    }
    function hideEditConstructSection() {
        storeSession('$ZDB_ident', '${publication.zdbID}', 'featcur_featediting', 'hide', '/action/curation/${publication.zdbID}');
        jQuery('#constructedit').hide();
        jQuery('#showConstructEdit').show();
        jQuery('#hideConstructEdit').hide();
    }
</script>