<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>


<!-- -->
<!-- This script is required bootstrap stuff. -->
<!-- You can put it in the HEAD, but startup -->
<!-- is slightly faster if you include it here. -->
<!-- -->

<script type="text/javascript">
    var LookupProperties = { NumLookups: "4" };
    var LookupProperties0 = {
        divName: "featureTerm",
        inputName: "searchTerm",
        showError: true,
        type: "FEATURE_LOOKUP",
        wildcard: true,
        useTermTable: false
    };

    var LookupProperties1 = {
        divName: "markerTerm",
        inputName: "searchTerm",
        showError: true,
        type: "MARKER_LOOKUP",
        wildcard: true,
        useTermTable: false
    };

    var LookupProperties2 = {
        divName: "antigenGene",
        inputName: "searchTerm",
        showError: true,
        action: "GENEDOM_AND_EFG_SEARCH",
        onclick: "hello",
        type: "GENEDOM_AND_EFG_LOOKUP",
        wildcard: false,
        useTermTable: false
    };

    var LookupProperties3 = {
        divName: "anatomyTerm",
        inputName: "searchTerm",
        showError: true,
        type: "GDAG_TERM_LOOKUP",
        ontologyName: "zebrafish_anatomy",
        wildcard: true,
        useTermTable: false
    };

</script>

<!-- OPTIONAL: include this if you want history support -->
<iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>

<h1>Lookup Application</h1>

<table>
    <tr>
        <td> Feature</td>
    </tr>
    <tr>
        <td>
            <div id="featureTerm"></div>
        </td>
    </tr>
    <tr>
        <td> General Marker</td>
    </tr>
    <tr>
        <td>
            <div id="markerTerm"></div>
        </td>
    </tr>
    <tr>
        <td> Gene or EFG</td>
    </tr>
    <tr>
        <td>
            <div id="antigenGene"></div>
        </td>
    </tr>
    <tr>
        <td> Anatomy Term</td>
    </tr>
    <tr>
        <td>
            <span id="anatomyTerm"></span>
        </td>
    </tr>
</table>
