<%@ page import="org.zfin.gwt.marker.ui.StandardMarkerDivNames" %>
<%@ page import="org.zfin.gwt.marker.ui.TranscriptEditController" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "${formBean.marker.zdbID}"
    } ;

</script>

<%--Adds the TranscriptEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>


<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <div id="<%=StandardMarkerDivNames.viewDiv%>"></div>
    </td></tr>
    <tr>
        <td>
            <div id="<%=StandardMarkerDivNames.headerDiv%>"></div>
            <br>
            <br>
            <div id="<%=StandardMarkerDivNames.directAttributionDiv%>"></div>
            <br>
            <div class="summaryTitle">Alias:</div>
            <div id="<%=StandardMarkerDivNames.previousNameDiv%>"></div>
        </td>
        <td valign="top">
            <div id="<%=StandardMarkerDivNames.publicationLookupDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="summaryTitle">Notes:</div>
            <div id="<%=StandardMarkerDivNames.noteDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div class="summaryTitle">Produced by Genes:</div>
            <div id="<%=StandardMarkerDivNames.geneDiv%>"></div>
        </td>
        <td colspan="1" valign="top">
            <div class="summaryTitle">Contained in Clone</div>
            <div id="<%=TranscriptEditController.cloneRelatedDiv%>"></div>
            <a target="_blank" href="/action/marker/clone-add" class="external">Add New Clone</a>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="summaryTitle"
                 id="<%=TranscriptEditController.targetedGenesTitle%>">Targeted Genes:</div>
            <div id="<%=TranscriptEditController.targetedGeneDiv%>"></div>
        </td>

    </tr>
    <tr>
        <td>
            <a name="proteinLookup"/>
            <div class="summaryTitle"
                 id="<%=TranscriptEditController.proteinTitle%>">Protein Products:</div>
            <div id="<%=TranscriptEditController.proteinDiv%>"></div>
            <div id="<%=TranscriptEditController.newProteinDiv%>"></div>
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
            <div id="<%=StandardMarkerDivNames.dbLinkDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <a name="sequence"></a>
            <div class="summaryTitle">RNA Sequences:</div>
            <div id="<%=TranscriptEditController.rnaDiv%>"></div>
        </td>
     </tr>
</table>

<%--</authz:authorize>--%>



