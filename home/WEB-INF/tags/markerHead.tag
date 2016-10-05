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
    <script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
    <script src="/javascript/nomenclature.js" type="text/javascript"></script>
</authz:authorize>

<script>
    markerID = '${marker.zdbID}';
</script>
<c:if test="${showEditControls}">
    <authz:authorize access="hasRole('root')">
        <caption>
            <div ng-click="eControl.editMarker()" id="editMarker" style="cursor: pointer;" class="error">Edit
            </div>
            <div ng-click="eControl.viewMarker()" style="display: none" id="viewMarker" style="cursor: pointer;">
                View
            </div>
        </caption>
        <div ng-controller="NomenclatureController as control">
    </authz:authorize>
</c:if>
<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${marker.markerType.displayName} Name:</span></th>
        <td>
            <span class="name-value"><zfin:name entity="${marker}"/></span>
            <authz:authorize access="hasRole('root')">
                <span style="cursor: pointer;"
                      ng-click="control.openGeneEditor('${marker.zdbID}','${marker.name}', 'Gene Name', false)"
                      ng-if="editMode">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red"></i>
                </span>
            </authz:authorize>
        </td>
    </tr>

    <zfin2:previousNamesFast previousNames="${previousNames}" showEditControls="true"/>
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

</div>


