<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

<table class="table table-borderless">
    <tr>
        <td>
            <div id="go-add-link"></div>
            <div id="go-evidence-add"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div id="go-evidence-display-filter"></div>
            <div id="go-evidence-display"></div>
        </td>
    </tr>
</table>
<!--
Add ontology name as a hidden field
-->
<div class="GO_TERM_single" aria-hidden="true" style="display: none;">
    cellular_component,molecular_function,biological_process
</div>


<script type="text/javascript" src="/javascript/zfin-gwt-autocomplete-helper.js">
</script>

<script type="text/javascript">
    var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "GO_CURATION",
        debug: "false"
    }
</script>

