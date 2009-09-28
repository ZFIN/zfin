<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID: "${formBean.zdbID}",
        accession: "${formBean.accession}",
        refDB: "${formBean.refDBName}"
    } ;

    var LookupProperties = { NumLookups: "1" } ;
    var LookupProperties0 = {
        //        divName: "featureTerm",
        //        inputName: "searchTerm",
        showError: true,
        buttonText: "Add",
        type: "MARKER_LOOKUP",
        showError: true,
        wildcard: false
    };

</script>

<link rel="stylesheet" type="text/css" href="/gwt/org.zfin.marker.presentation.Marker/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.marker.presentation.Marker/org.zfin.marker.presentation.Marker.nocache.js"></script>

<input type="hidden" name="hiddenName" id="hiddenName"/>


<table cellpadding="10">
    <tr>
        <td>
            <div id="proteinName"></div>
        </td>
        <td valign="top">
            <div id="publicationName"></div>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <a name="geneLookup"/>
            <b>Producing Genes:</b>
            <div id="geneName"></div>
            <div id="geneLookupName"></div>
            <br>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <b>Producing Transcripts:</b>
            <div id="transcriptName"></div>
        </td>
        <td>
            <div id="newSequenceName"></div>
            <div id="viewSequenceName"></div>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



