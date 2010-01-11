<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/Marker.css"/>


<%--<authz:authorize ifAnyGranted="root">--%>

<%
    String zdbID = request.getParameter("zdbID");
    if(zdbID==null){
        zdbID = "ZDB-TSCRIPT-090929-6229" ;
    }
    String personID = request.getParameter("personID");
    if(personID==null){
        personID = "ZDB-PERS-960805-676";
    }
%>


<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "<%= zdbID %>",
    } ;

</script>

<%--Adds the TranscriptEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<input type="hidden" name="hiddenName" id="hiddenName"/>



<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <div id="viewTranscript"></div>
    </td></tr>
    <tr>
        <td>
            <div id="markerName"></div>
            <div id="directAttributionName"></div>
            <br>
            <div class="summaryTitle">Previous Name(s):</div>
            <div id="aliasName"></div>
        </td>
        <td valign="top">
            <div id="publicationName"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div class="summaryTitle">Curator Notes:</div>
            <div id="curatorNoteName"></div>
        </td>
        <td>
            <div class="summaryTitle">Public Notes:</div>
            <div id="publicNoteName"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div class="summaryTitle">Produced by Genes:</div>
            <div id="geneName"></div>
        </td>
        <td colspan="1" valign="top">
            <div class="summaryTitle">Contained in Clone</div>
            <div id="cloneRelatedName"></div>
            <a target="_blank" href="/action/marker/clone-add" class="external">Add New Clone</a>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="summaryTitle"
                 id="targetedGeneTitle">Targeted Genes:</div>
            <div id="targetedGeneName"></div>
        </td>

    </tr>
    <tr>
        <td>
            <a name="proteinLookup">
            <div class="summaryTitle"
                 id="proteinTitle">Protein Products:</div>
            <div id="proteinName"></div>
            <div id="newProteinName"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <hr>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="summaryTitle">Supporting Sequences:</div>
            <div id="dbLinksName"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <a name="sequence"></a>
            <div class="summaryTitle">RNA Sequences:</div>
            <div id="rnaName"></div>
            <!--<br>-->
            <!--<div id="rnaUpdateName"></div>-->
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>


