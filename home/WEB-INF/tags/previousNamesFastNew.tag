<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="false" %>
<%@ attribute name="showEditControls" type="java.lang.Boolean" rtexprvalue="true" required="false" %>
<%@ attribute name="label" rtexprvalue="true" required="false"
              description="if nothing is specified, Synonyms: will be used" %>

<td>
            <span id="previousNameListOriginal">
            <c:forEach var="markerAlias" items="${previousNames}" varStatus="loop">
                <span id="previous-name-${loop.index}">${markerAlias.linkWithAttribution}</span><c:if
                    test="${showEditControls}"><authz:authorize
                    access="hasRole('root')"><span
                    style="cursor: pointer;"
                    ng-click="control.editAttribution('${markerAlias.aliasZdbID}','${markerAlias.pureAliasName}')"
                    ng-if="editMode">
                    <i class="far fa-edit" aria-hidden="true" style="color: red"
                       title="Edit attributions on alias"></i>
                </span><span style="cursor: pointer;"
                             ng-click="control.confirmDeleteAlias('${markerAlias.aliasZdbID}','${markerAlias.alias}')"
                             ng-if="editMode">
                    <i class="fas fa-trash" aria-hidden="true"
                       style="color: red"
                       title="Delete alias and its attributions"></i></span></authz:authorize></c:if>${(!loop.last ?", " : "")}
            </c:forEach>
                </span>
</td>




