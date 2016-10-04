<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="false" %>

<authz:authorize access="hasRole('root')">
    <script src="/javascript/nomenclature.js" type="text/javascript"></script>
    <script>
        var reasonList = [];
        <c:forEach items="${gene.markerHistory.iterator().next().reasonArray}" var="reason" varStatus="status">
        reasonList.push('${reason.toString()}');
        </c:forEach>
    </script>

    <script>
        markerID = '${gene.zdbID}';
    </script>
    <caption>
        <div ng-click="eControl.editMarker()" id="editMarker" style="cursor: pointer;" class="error">Edit</div>
        <div ng-click="eControl.viewMarker()" style="display: none" id="viewMarker" style="cursor: pointer;">
            View
        </div>
    </caption>
    <div ng-controller="NomenclatureController as control">
</authz:authorize>
<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${gene.markerType.displayName} Name:</span></th>
        <td>
            <span class="name-value"><zfin:name entity="${gene}"/></span>
            <authz:authorize access="hasRole('root')">
                <span style="cursor: pointer;"
                      ng-click="control.openGeneEditor('${gene.zdbID}','${gene.name}', 'Gene Name')"
                      ng-if="editMode">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red" title="Edit gene name"></i>
                </span>
            </authz:authorize>
        </td>
    </tr>
    <tr>
        <th><span class="name-label">${gene.markerType.displayName} Symbol:</span></th>
        <td>
            <span class="name-value" geneSymbol><zfin:abbrev entity="${gene}"/></span>
            <authz:authorize access="hasRole('root')">
                    <span style="cursor: pointer;"
                          ng-click="control.openGeneEditor('${gene.zdbID}','${gene.abbreviation}', 'Gene Symbol')"
                          ng-if="editMode">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red" title="Edit gene symbol"></i></span>
            </authz:authorize>
        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <zfin2:nomenclature geneEdit="true" showReason="${gene.type.geneOrGenep}"/>
        </td>
    </tr>
    <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}" marker="${gene}" showEditControls="true"/>
    <tr>
        <th>Location:</th>
        <td>
            <zfin2:displayLocation entity="${gene}" longDetail="true"/>
        </td>
    </tr>
    <c:if test="${formBean.hasMarkerHistory}">
        <tr>
            <td colspan="2">
                <a class="data-note" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature
                    History</a>
            </td>
        </tr>
    </c:if>

    <zfin2:entityNotes entity="${gene}"/>

</table>
<authz:authorize access="hasRole('root')">
    </div>
</authz:authorize>




