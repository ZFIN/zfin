<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="zdbID" type="java.lang.String"
              rtexprvalue="true" required="false" %>
<%@ attribute name="editURL" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="editLinkText" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="deleteURL" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="mergeURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="trackURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="oboID" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="termID" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="rtype" type="java.lang.String" rtexprvalue="true" description="Needed for linking to updates apg" %>
<%@ attribute name="showLastUpdate" type="java.lang.Boolean" rtexprvalue="true" required="false" description="Should the Last Updated link show?" %>

<%@ attribute name="isOwner" type="java.lang.Boolean" rtexprvalue="true" description="Determines if owner."
              required="false" %>


<%-- default showLastUpdate to false --%>
<c:set var="showLastUpdate" value="${(empty showLastUpdate) ? true : showLastUpdate}" />


<table class="data_manager">
    <tbody>
    <tr>
        <c:if test="${!empty zdbID}">
            <td>
                <b>ZFIN ID:</b> ${zdbID}
            </td>
        </c:if>
        <c:if test="${!empty oboID}">
            <td>
                <b>OBO ID:</b> ${oboID}
            </td>
        </c:if>
        <c:if test="${!empty termID}">
            <authz:authorize ifAnyGranted="root">
                <td>
                    <b>Term ID:</b> ${termID}
                </td>
            </authz:authorize>
        </c:if>

        <authz:authorize ifAnyGranted="root">
            <zfin2:dataManagerPrivileged zdbID="${zdbID}" editURL="${editURL}" editLinkText="${editLinkText}" deleteURL="${deleteURL}"
                                         mergeURL="${mergeURL}" trackURL="${trackURL}" oboID="${oboID}" rtype="${rtype}" showLastUpdate="${showLastUpdate}"/>
        </authz:authorize>
        <authz:authorize ifNotGranted="root" ifAnyGranted="submit">
            <c:if test="${isOwner}">
                <zfin2:dataManagerPrivileged zdbID="${zdbID}" editURL="${editURL}" deleteURL="${deleteURL}"
                                             mergeURL="${mergeURL}" trackURL="${trackURL}" oboID="${oboID}" rtype="${rtype}" showLastUpdate="${showLastUpdate}"/>
            </c:if>
        </authz:authorize>

    </tr>
    </tbody>
</table>

