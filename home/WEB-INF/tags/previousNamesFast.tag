<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="false" %>
<%@ attribute name="label" rtexprvalue="true" required="false"
              description="if nothing is specified, Synonyms: will be used" %>

<c:if test="${empty label}">
    <c:set var="label" value="Synonym"/>
</c:if>

<c:choose>
    <c:when test="${fn:length(previousNames) > 1}">
        <c:set var="label" value="${label}s:"/>
    </c:when>
    <c:otherwise>
        <c:set var="label" value="${label}:"/>
    </c:otherwise>
</c:choose>

<tr>
    <th>
        ${label}
        <authz:authorize access="hasRole('root')">
                     <span style="cursor: pointer;"
                           ng-click="control.openAddNewPreviousNameEditor()"
                           ng-if="control.editMode">
                         <i style="color: red" title="Create a new previous name">New</i>
                         </span>
        </authz:authorize>
    </th>
    <td>
            <span id="previousNameListOriginal">
            <c:forEach var="markerAlias" items="${previousNames}" varStatus="loop">
                <span id="previous-name-${loop.index}">${markerAlias.linkWithAttribution}</span><authz:authorize
                    access="hasRole('root')"><span
                    style="cursor: pointer;"
                    ng-click="control.editAttribution('${markerAlias.aliasZdbID}','${markerAlias.pureAliasName}')"
                    ng-if="control.editMode">
                    <i class="fa fa-pencil" aria-hidden="true" style="color: red"
                       title="Edit attributions on alias"></i>
                </span><span style="cursor: pointer;"
                             ng-click="control.confirmDeleteAlias('${markerAlias.aliasZdbID}','${markerAlias.alias}')"
                             ng-if="control.editMode">
                    <i class="fa fa-trash" aria-hidden="true"
                       style="color: red"
                       title="Delete alias and its attributions"></i></span></authz:authorize>${(!loop.last ?", " : "")}
            </c:forEach>
                </span>
        <authz:authorize access="hasRole('root')">
    <span id="previousNameList>" ng-repeat="previousNameItem in control.previousNameList ">
                 <span ng-bind-html="previousNameItem.attributionLink | unsafe"></span><span style="cursor: pointer;"
                                                                                             ng-click="control.editAttribution(previousNameItem.aliasZdbID, previousNameItem.alias)"
                                                                                             ng-if="control.editMode">
                    <i class="fa fa-pencil" aria-hidden="true" style="color: red"></i> </span><span
            style="cursor: pointer;"
            ng-click="control.confirmDeleteAlias(previousNameItem.aliasZdbID, previousNameItem.alias)"
            ng-if="control.editMode">
                    <i class="fa fa-trash" aria-hidden="true" style="color: red"
                       title="Delete alias and its attributions"></i></span>{{$last ? '' : ', '}}
            </span>
        </authz:authorize>
    </td>
</tr>
<tr>
    <td>
        <zfin2:addNewAlias/>
    </td>
</tr>




