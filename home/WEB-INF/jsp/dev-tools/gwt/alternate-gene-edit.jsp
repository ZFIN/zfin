<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="GWT Gene Edit">
    <%--<authz:authorize access="hasRole('root')">--%>
    <c:set var="zdbID" value="${param.zdbID}" />
    <c:if test="${empty zdbID}">
        <c:set var="zdbID" value="AlternateZDB-GENE-001103-2" />
    </c:if>
    <script type="text/javascript">
        var MarkerProperties= {
            zdbID : "${zdbID}"
        } ;

    </script>

    <%--Adds the AlternateGeneEditController.--%>
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
                <div id="${StandardDivNames.geneDiv}"></div>
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
        <tr>
            <td colspan="2">
                <div id="${StandardDivNames.directAttributionDiv}"></div>
                <br>
            </td>
        </tr>
    </table>

    <%--</authz:authorize>--%>
</z:devtoolsPage>

