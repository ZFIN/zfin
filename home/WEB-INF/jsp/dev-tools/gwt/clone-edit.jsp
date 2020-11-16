<%@ page import="org.zfin.gwt.marker.ui.CloneEditController" %>
<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="GWT Clone Edit">
    <%--<authz:authorize access="hasRole('root')">--%>
    <c:set var="zdbID" value="${param.zdbID}" />
    <c:if test="${empty zdbID}">
        <c:set var="zdbID" value="ZDB-BAC-050218-3625" />
    </c:if>

    <script type="text/javascript">
        var MarkerProperties= {
            zdbID : "${zdbID}"
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
                <div id="${StandardDivNames.supplierDiv}"></div>
                <br>
                <div id="${StandardDivNames.directAttributionDiv}"></div>
                <br>
                <b>Previous Name(s):</b>
                <div id="${StandardDivNames.previousNameDiv}"></div>
            </td>
            <td valign="top">
                <div id="${StandardDivNames.publicationLookupDiv}"></div>
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
            <td>
                <div id="${CloneEditController.genesTitle}"></div>
                <div id="${StandardDivNames.geneDiv}"></div>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <h3>Clone Data</h3>
                <div id="${StandardDivNames.dataDiv}"></div>
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
                <div id="${StandardDivNames.dbLinkDiv}"></div>
            </td>
        </tr>
    </table>

    <%--</authz:authorize>--%>
</z:devtoolsPage>