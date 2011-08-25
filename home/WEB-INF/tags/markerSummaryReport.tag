<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="links" type="java.util.List"
              rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="title" type="java.lang.String" rtexprvalue="true" required="false" %>
<%-- in the app page, the html for this is a table with list items floating on their
     own, one per cell.  it's kind of odd.  Maybe it can actually be a list, just inline
     and not allowing any wrapping --%>

<c:if test="${empty title}">
    <c:set var="title">
        OTHER <zfin:abbrev entity="${marker}"/>
        ${marker.markerType.displayName eq 'cDNA' ? marker.markerType.displayName : fn:toUpperCase(marker.markerType.displayName)}
        PAGES
    </c:set>
</c:if>


<zfin2:subsection title="${title}"
                  test="${!empty links}" showNoData="true" noDataText="No links to external sites">
    <table class="summary">
        <tr>
                <%--<c:forEach var="link" items="${links}" varStatus="loop">--%>
                <%--${(!loop.first && loop.index%3==0 ? "<td>" : "")}--%>
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
                    <%--</td>--%>
                    <%--${(!loop.first && loop.index%3==0 ? "</td>" : "")}--%>
        </tr>
    </table>
</zfin2:subsection>

