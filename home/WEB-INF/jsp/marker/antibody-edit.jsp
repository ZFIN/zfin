<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <script>
        if (opener != null)
            opener.fireCreateMarkerEvent();
    </script>

    <script type="text/javascript">
        var MarkerProperties= {
            zdbID : "${formBean.marker.zdbID}",
            antibodyDefPubZdbID: "${param.antibodyDefPubZdbID}"
        } ;

    </script>

    <%--Adds the CloneEditController.--%>
    <script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

    <table cellpadding="10">
        <tr><td align="center" colspan="2">
            <div id="${StandardDivNames.viewDiv}"></div>
        </td></tr>
        <tr>
            <td>
                <div id="${StandardDivNames.headerDiv}"></div>
                <br>
                <b>Alias:</b>
                <div id="${StandardDivNames.previousNameDiv}"></div>
                <br>
                <br>
                <b>Antibody Registry:</b>
                <div id="${StandardDivNames.abRegistryDiv}"></div>
                <br>
            </td>
            <td valign="top">
                <div id="${StandardDivNames.publicationLookupDiv}"></div>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <h3>Antibody Data</h3>
                <div id="${StandardDivNames.dataDiv}"></div>
            </td>
        </tr>
        <tr>
            <td>
                <b>Antigen Genes:</b>
                <div id="${StandardDivNames.geneDiv}"></div>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <b>Notes:</b>
                <br>
                <div id="${StandardDivNames.noteDiv}"></div>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <b>Suppliers:</b>
                <br>
                <div id="${StandardDivNames.supplierDiv}"></div>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <div id="${StandardDivNames.directAttributionDiv}"></div>
            </td>
        </tr>
    </table>

    <%--</authz:authorize>--%>
</z:page>


