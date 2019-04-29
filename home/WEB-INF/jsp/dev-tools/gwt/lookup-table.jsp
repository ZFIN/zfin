<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>


<!-- -->
<!-- This script is required bootstrap stuff. -->
<!-- You can put it in the HEAD, but startup -->
<!-- is slightly faster if you include it here. -->
<!-- -->

<script type="text/javascript">
    var LookupProperties = {
        divName: "anatomyTermTable",
        inputName: "searchTerm",
        showError: true,

        hiddenNames: "antibodyCriteria.anatomyTermNames",
        hiddenIds: "antibodyCriteria.anatomyTermIDs",
        type: "GDAG_TERM_LOOKUP",
        ontologyName: "zebrafish_anatomy",
        width: 40,
        wildcard: false,
        useTermTable: true
    }
</script>

<input id="antibodyCriteria.anatomyTermIDs" name="antibodyCriteria.anatomyTermIDs" type="hidden">
<input id="antibodyCriteria.anatomyTermNames" name="antibodyCriteria.anatomyTermNames" type="hidden">

<h1>Lookup Table Application</h1>

<form name="lookupTable" action="run">
    <table>
        <tr>
            <td>Anatomy Term Table</td>
        </tr>
        <tr>
            <td>
                <div id="anatomyTermTable"></div>
            </td>
        </tr>
    </table>
</form>