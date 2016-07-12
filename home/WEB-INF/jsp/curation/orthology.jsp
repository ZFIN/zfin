<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/ortho-edit.js"></script>
<script src="/javascript/inline-edit-textarea.directive.js"></script>

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
            <div ng-app="app">
                <div ortho-edit pub="${publication.zdbID}"></div>
            </div>
        </td>
    </tr>
</table>