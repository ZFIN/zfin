<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
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
        buttonText: "search",
        type: "ANATOMY_ONTOLOGY",
        useTermTable: true,
        wildcard: false
    };
</script>

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