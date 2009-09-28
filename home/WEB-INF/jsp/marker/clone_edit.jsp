<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "${formBean.marker.zdbID}",
        curatorID : "${formBean.user.zdbID}"
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

<%--Adds the CloneEditController.--%>
<link rel="stylesheet" type="text/css" href="/gwt/org.zfin.marker.presentation.Marker/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.marker.presentation.Marker/org.zfin.marker.presentation.Marker.nocache.js"></script>

<input type="hidden" name="hiddenName" id="hiddenName"/>

<table cellpadding="10">
    <tr><td align="center" colspan="2">
        <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&OID=${formBean.marker.zdbID}">[View Clone]</a>
    </td></tr>
    <tr>
        <td>
            <div id="markerName"></div>
            <br>
            <div id="supplierName"></div>
            <br>
            <div id="directAttributionName"></div>
            <br>
            <b>Previous Name(s):</b>
            <div id="aliasName"></div>
        </td>
        <td valign="top">
            <div id="publicationName"></div>
        </td>
    </tr>
    <tr>
        <td>
            <b>Curator Notes:</b>
            <br>
            <div id="curatorNoteName"></div>
        </td>
        <td>
            <b>Public Notes:</b>
            <div id="publicNoteName"></div>
        </td>
    </tr>
    <tr>
        <td>
            <b>Contains Genes:</b>
            <div id="geneName"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <h3>Clone Data</h3>
            <div id="cloneDataName"></div>
            <div id="cloneName"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <hr>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b>Sequences:</b>
            <br>
            <div id="dbLinksName"></div>
        </td>
    </tr>
</table>

<%--</authz:authorize>--%>



