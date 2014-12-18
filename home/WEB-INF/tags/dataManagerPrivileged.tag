<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="zdbID" type="java.lang.String"
              rtexprvalue="true" required="false" %>
<%@ attribute name="editURL" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="deleteURL" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="mergeURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="oboID" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="rtype" type="java.lang.String" rtexprvalue="true" description="Needed for linking to updates apg" %>

<c:if test="${!empty editURL}">
    <td>
        <a href="${editURL}" class="root">Edit</a>
    </td>
</c:if>
<c:if test="${!empty deleteURL
                  and deleteURL ne 'none'
                  and
                  (!empty oboID ? !fn:startsWith('ZDB-TSCRIPT-',oboID) : true)
                  }">
    <td>
        <a href="javascript:;" class="root" onclick="location.replace('${deleteURL}');">Delete</a>
    </td>
</c:if>
<c:if test="${!empty mergeURL}">
    <td>
            <%--<a href="javascript:;" class="root" onclick="confirmMerge();">Merge</a>--%>
        <a href="${mergeURL}" class="root">Merge</a>
    </td>
</c:if>

<%-- I'm not sure how sound this logic is, but I'm gonna say if no rtype is passed in, don't
even try to look for a last update..  (it could also be an explicit flag)--%>
<c:if test="${!empty rtype}">
    <td>
        <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-update-vframeset.apg&OID=${zdbID}&rtype=${rtype}">

            Last Update:
            <c:set var="latestUpdate" value="${zfn:getLastUpdate(zdbID)}"/>
            <c:choose>
                <c:when test="${!empty latestUpdate}">
                    <fmt:formatDate value="${latestUpdate.dateUpdated}" type="date"/>
                </c:when>
                <c:otherwise>
                    Never modified
                </c:otherwise>
            </c:choose>
        </a>
    </td>
</c:if>


