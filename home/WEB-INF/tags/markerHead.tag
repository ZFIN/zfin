<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker"
              rtexprvalue="true" required="true" %>
<%@ attribute name="typeName" type="java.lang.String" required="false" rtexprvalue="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="showEditControls" required="true" %>

<c:if test="${empty typeName}">
    <c:set var="typeName">${marker.markerType.displayName}</c:set>
</c:if>

<authz:authorize access="hasRole('root')">
    <script>
        markerID = '${marker.zdbID}';

        var reasonList = [];
        <c:forEach items="${markerHistoryReasonCodes}" var="reason" varStatus="status">
        reasonList.push('${reason.toString()}');
        </c:forEach>
    </script>

    <c:if test="${showEditControls}">
        <caption>
            <div ng-click="eControl.editMarker()" ng-if="!editMode" style="cursor: pointer;" class="error">Edit</div>
            <div ng-click="eControl.viewMarker()" ng-if="editMode" style="cursor: pointer;" class="error">
                View
            </div>
        </caption>
    </c:if>
    <div ng-controller="NomenclatureController as control">
</authz:authorize>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${marker.markerType.displayName} Name:</span></th>
        <td>
            <span class="name-value"><zfin:name entity="${marker}"/></span>
            <c:if test="${showEditControls}">
                <authz:authorize access="hasRole('root')">
                <span style="cursor: pointer;"
                      ng-click="control.openGeneEditor('${marker.zdbID}', control.geneName, 'Gene Name', false)"
                      ng-if="editMode">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red"></i>
                </span>
                </authz:authorize>
            </c:if>
        </td>
    </tr>

    <c:if test="${showEditControls}">
        <zfin2:previousNamesFast previousNames="${previousNames}" showEditControls="true"/>
    </c:if>
    <c:if test="${!showEditControls}">
        <zfin2:previousNamesFast previousNames="${previousNames}" showEditControls="false"/>
    </c:if>
    <c:if test="${formBean.marker.type ne 'EFG'&& formBean.marker.type ne 'REGION'&& !(fn:contains(formBean.marker.type,'CONSTRCT'))}">
        <%--<c:if test="${formBean.marker.type ne 'REGION'}">--%>
        <tr>
            <th>Location:</th>
            <td>
                <zfin2:displayLocation entity="${formBean.marker}"/>
            </td>
        </tr>
    </c:if>
    <%--</c:if>--%>
    <zfin2:entityNotes entity="${formBean.marker}"/>

</table>
<zfin2:nomenclature geneEdit="true" showReason="false"/>





