<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ page import="org.zfin.gwt.marker.ui.TranscriptEditController" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>

<%
    String zdbID = request.getParameter("zdbID");
    if(zdbID==null){
        zdbID = "ZDB-TSCRIPT-090929-6229" ;
    }
%>


<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "<%= zdbID %>"
    } ;

</script>

<%--Adds the TranscriptEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <div id="<%=StandardDivNames.viewDiv%>"></div>
    </td></tr>
    <tr>
        <td>
            <div id="<%=StandardDivNames.headerDiv%>"></div>
            <br>
            <br>
            <div id="<%=StandardDivNames.directAttributionDiv%>"></div>
            <br>
            <div class="summaryTitle">Alias:</div>
            <div id="<%=StandardDivNames.previousNameDiv%>"></div>
        </td>
        <td valign="top">
            <div id="<%=StandardDivNames.publicationLookupDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="summaryTitle">Notes:</div>
            <div id="<%=StandardDivNames.noteDiv%>"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div class="summaryTitle">Produced by Genes:</div>
            <div id="<%=StandardDivNames.geneDiv%>"></div>
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
            <div id="<%=StandardDivNames.dbLinkDiv%>"></div>
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


