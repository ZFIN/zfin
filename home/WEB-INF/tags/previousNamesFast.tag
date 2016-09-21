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

<script>
    markerID = '${marker.zdbID}';
</script>

<c:if test="${!empty previousNames}">
    <tr>
        <th>
                ${label}
                     <span style="cursor: pointer;"
                           ng-click="control.openAddNewPreviousNameEditor()">
                         <i style="color: red">New</i>
                         </span>
        </th>
        <td>
            <span id="previousNameListOriginal">
            <c:forEach var="markerAlias" items="${previousNames}" varStatus="loop">
                <span id="previous-name-${loop.index}">${markerAlias.linkWithAttribution}</span>
                                <span style="cursor: pointer;"
                                      ng-click="control.editAttribution('${markerAlias.aliasZdbID}','${markerAlias.pureAliasName}')">
                    <i class="fa fa-pencil" aria-hidden="true" style="color: red"></i>
                </span>
                                <span style="cursor: pointer;"
                                      ng-click="control.deleteAlias('${markerAlias.aliasZdbID}','${markerAlias.markerZdbID}')">
                    <i class="fa fa-trash" aria-hidden="true" style="color: red"></i>
                </span>
                ${(!loop.last ?", " : "")}

            </c:forEach>
                </span>
            <span id="previousNameList>" ng-repeat="previousNameItem in control.previousNameList ">
                             <span ng-bind-html="previousNameItem.alias | unsafe"></span>  {{previousNameItem.attributionLink}}
                                <span style="cursor: pointer;"
                                      ng-click="control.editAttribution(previousNameItem.aliasZdbID, previousNameItem.alias)">
                    <i class="fa fa-pencil" aria-hidden="true" style="color: red"></i> </span>
                                <span style="cursor: pointer;"
                                      ng-click="control.deleteAlias(previousNameItem.aliasZdbID)">
                    <i class="fa fa-trash" aria-hidden="true" style="color: red"></i>
            </span>{{$last ? '' : ', '}}
            </span>
        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <zfin2:addNewAlias/>
        </td>
    </tr>
</c:if>




