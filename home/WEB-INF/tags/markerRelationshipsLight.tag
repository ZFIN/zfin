<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="relationships" required="true"
              rtexprvalue="true" type="java.util.List" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="showEditControls" type="java.lang.Boolean" rtexprvalue="true" required="false" %>

<%@ attribute name="title" required="false" %>


<c:if test="${empty title}">
    <c:set var="title" value="MARKER RELATIONSHIPS"/>
</c:if>
<script>
    markerID = '${marker.zdbID}';

    var relnTypeList = [];
    <c:forEach items="${markerRelationshipTypes}" var="reln" varStatus="status">
    relnTypeList.push('${reln.toString()}');
    </c:forEach>
</script>

<tr>
    <th>
        ${title}
        <c:if test="${showEditControls}">
            <authz:authorize access="hasRole('root')">
                     <span style="cursor: pointer;"
                           ng-click="control.openAddNewRelationships()"
                           ng-if="editMode">
                         <i style="color: red" title="Create a new marker relationship">New</i>
                         </span>
            </authz:authorize>
        </c:if>
    </th>

<zfin2:subsection title="${title}"
                  test="${!empty relationships}" showNoData="true">

    <table class="summary horizontal-solidblock">
        <c:set var="relationshipType" value="notthesame"/>
        <c:set var="markerType" value="notthesame"/>

        <c:forEach var="entry" items="${relationships}" varStatus="loop">

        <c:if test="${entry.relationshipType ne relationshipType}">
        <c:if test="${!loop.first}">
            </td>
            </tr>
        </c:if>
        <tr>
            <td class="data-label">
                    ${fn:startsWith(marker.zdbID,'ZDB-GENE') ||fn:startsWith(marker.zdbID,'ZDB-EFG')  ?"<span class=genedom>" : " "}
                    ${marker.abbreviation}

                    ${fn:startsWith(marker.zdbID,'ZDB-GENE') || fn:startsWith(marker.zdbID,'ZDB-EFG') ?"</span>" : " "}
                        ${entry.relationshipType}:

            </td>
            <td>
                </c:if>

                    <c:set var="suppressComma" value="false"/>
                <c:if test="${!loop.last}">
                    <c:set var="suppressComma"
                           value="${
                           entry.markerType ne relationships[loop.index+1].markerType
                    or entry.relationshipType ne relationships[loop.index+1].relationshipType
                    }"/>
                </c:if>


                <c:if test="${entry.markerType ne markerType or entry.relationshipType ne relationshipType }">
                    ${entry.relationshipType eq relationshipType ? "<br>" : ""}
                [${entry.markerType}]
                </c:if>
                    ${entry.linkWithAttributionAndOrderThis}${!loop.last
                        and !suppressComma ? ", ": ""}

                    <c:set var="relationshipType" value="${entry.relationshipType}"/>
                    <c:set var="markerType" value="${entry.markerType}"/>
                </c:forEach>
    </table>

</zfin2:subsection>

