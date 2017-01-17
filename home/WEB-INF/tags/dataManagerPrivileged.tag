<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="zdbID" type="java.lang.String"
              rtexprvalue="true" required="false" %>
<%@ attribute name="editURL" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="editLinkText" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="deleteURL" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="mergeURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="trackURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="linkURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="curateURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="viewURL" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="oboID" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="showLastUpdate" type="java.lang.Boolean" rtexprvalue="true" required="false"
              description="Should the Last Updated: xxxx link show?" %>
<%@ attribute name="isOwner" type="java.lang.Boolean" rtexprvalue="true" description="Determines if owner."
              required="false" %>
<%@ attribute name="editMarker" type="java.lang.Boolean" rtexprvalue="true" description="This is the marker edit link"
              required="false" %>

<c:if test="${!empty viewURL}">
    <td>
        <a href="${viewURL}" class="root">View</a>
    </td>
</c:if>
<c:if test="${!empty editURL}">
    <td>
        <a href="${editURL}" class="root">
            <c:choose>
                <c:when test="${empty editLinkText}">Edit</c:when>
                <c:otherwise>${editLinkText}</c:otherwise>
            </c:choose>
        </a>
    </td>
</c:if>
<c:if test="${editMarker}">
    <td>
        <div ng-click="eControl.editMarker()" ng-if="!editMode" style="cursor: pointer;" class="error">Edit</div>
        <div ng-click="eControl.viewMarker()" ng-if="editMode" style="cursor: pointer;" class="error">View</div>
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
<c:if test="${!empty trackURL}">
    <td>
        <a href="${trackURL}" class="root">Track</a>
    </td>
</c:if>
<c:if test="${!empty linkURL}">
    <td>
        <a href="${linkURL}" class="root">Link</a>
    </td>
</c:if>
<c:if test="${!empty curateURL}">
    <td>
        <a href="${curateURL}" class="root">Curate</a>
    </td>
</c:if>


<c:if test="${showLastUpdate}">
    <td>
        <a href="/action/updates/${zdbID}">

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


