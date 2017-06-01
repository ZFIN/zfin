<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="relationships" required="true"
              rtexprvalue="true" type="java.util.List" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="title" required="false" %>


<c:set var="loggedIn">no</c:set>


<authz:authorize access="hasRole('root')">
    <c:set var="loggedIn" value="true"/>
</authz:authorize>




   <zfin2:subsection title="${title}"
                  test="${!empty relationships}" showNoData="true">

    <table class="summary horizontal-solidblock">
        <c:set var="relationshipType" value="notthesame"/>
        <c:set var="markerType" value="notthesame"/>

        <c:forEach var="entry" items="${relationships}" varStatus="loop">

        <c:if test="${entry.relationshipType eq 'interacts with'}">
        <c:if test="${entry.relationshipType ne relationshipType}">
        <c:if test="${!loop.first}">
            </td>
            </tr>
        </c:if>
        <tr>
            <td class="data-label">
                    ${fn:startsWith(marker.zdbID,'ZDB-GENE') ||fn:startsWith(marker.zdbID,'ZDB-EFG')||fn:contains(marker.zdbID,'RNAG')  ?"<span class=genedom>" : " "}
                    ${marker.abbreviation}

                    ${fn:startsWith(marker.zdbID,'ZDB-GENE') || fn:startsWith(marker.zdbID,'ZDB-EFG') ||fn:contains(marker.zdbID,'RNAG') ?"</span>" : " "}
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
                    </c:if>
                </c:forEach>
    </table>
    <tr>

    </tr>

</zfin2:subsection>

