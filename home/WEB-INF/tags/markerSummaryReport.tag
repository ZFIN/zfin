<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="links" type="java.util.List"
              rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="title" type="java.lang.String" rtexprvalue="true" required="false" %>

<c:if test="${empty title}">
    <c:set var="title">
        OTHER&nbsp;<zfin:abbrev entity="${marker}"/>&nbsp;${marker.markerType.displayName eq 'cDNA' ? marker.markerType.displayName : fn:toUpperCase(marker.markerType.displayName)}&nbsp;PAGES
    </c:set>
</c:if>

<c:set var="loggedIn">no</c:set>

<authz:authorize access="hasRole('root')">
    <c:set var="loggedIn">yes</c:set>
</authz:authorize>

<c:if test="${loggedIn eq 'yes' && marker.genedom}">
    <div class="summary" ng-if="editMode">
        <span class="summaryTitle">${title}</span>
        <other-markers marker-id="${marker.zdbID}" edit="1">
        </other-markers>
    </div>
    <div class="summary" ng-if="!editMode">
        <span class="summaryTitle">${title}</span>
        <other-markers marker-id="${marker.zdbID}" edit="0">
        </other-markers>
    </div>
</c:if>

<c:if test="${loggedIn eq 'no' || !marker.genedom}">
    <zfin2:subsection title="${title}" anchor="other_pages"
                      test="${!empty links}" showNoData="true" noDataText="No links to external sites">
        <table class="summary">
            <tr>
                <td>
                        <c:set var="lastRefDB" value=""/>
                    <c:forEach var="link" items="${links}" varStatus="loop">
                        <c:set var="refDB" value="${link.referenceDatabaseName}"/>
                        ${(!loop.first and (refDB ne lastRefDB) ? "</td>" : "")}
                        ${(!loop.first and (refDB ne lastRefDB) ? "<td>" : "")}
                        <%-- entry.value is the MarkerDBLink --%>
                    <li style="list-style-type:none;">
                        <a href="${link.link}">${link.displayName}</a>
                            ${link.attributionLink}
                    </li>
                        <c:set var="lastRefDB" value="${refDB}"/>
                    </c:forEach>
            </tr>
        </table>
    </zfin2:subsection>
</c:if>

