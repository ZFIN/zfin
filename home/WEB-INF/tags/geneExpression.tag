<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="relationships" required="true"
              rtexprvalue="true" type="java.util.List" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="MARKER RELATIONSHIPS"/>
</c:if>

<zfin2:subsection title="${title}"
                  test="${!empty relationships}">

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
                    <td>${marker.abbreviation} ${entry.relationshipType}</td>
                    <td>
            </c:if>

             <c:if test="${entry.markerType ne markerType}">
                 ${entry.relationshipType eq relationshipType ? "<br>" : ""}
                 [${entry.markerType}]
             </c:if>

              ${entry.link}

              ${entry.attributionLink}

              ${entry.orderThisLink}
                    <%--${entry.supplier}--%>
              <%--(<a href="${entry}">1</a>)--%>

              <c:set var="relationshipType" value="${entry.relationshipType}"/>
              <c:set var="markerType" value="${entry.markerType}"/>
         </c:forEach>
    </table>

</zfin2:subsection>

