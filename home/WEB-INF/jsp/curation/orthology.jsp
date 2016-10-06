<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/ortho-edit.js"></script>
<script src="/javascript/inline-edit-textarea.directive.js"></script>
<script>
    // we want the jquery modal, not the bootstrap one on this page
    $.fn.modal.noConflict();
</script>

<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<script type="text/javascript">
    var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "ORTHO_TAB",
        debug: "false"
    };
</script>
<?/MIVAR>

<table class="table table-borderless">
    <tr>
        <td>
            <div id="directAttributionName"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div ortho-edit pub="${publication.zdbID}"></div>
        </td>
    </tr>
</table>