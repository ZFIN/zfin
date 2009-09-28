<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="links" type="org.zfin.marker.presentation.SummaryDBLinkDisplay"
              rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="title" type="java.lang.String" rtexprvalue="true" required="false" %>
<%-- in the app page, the html for this is a table with list items floating on their
     own, one per cell.  it's kind of odd.  Maybe it can actually be a list, just inline
     and not allowing any wrapping --%>

<c:if test="${empty title}">
    <c:set var="title">
        OTHER <zfin:abbrev entity="${marker}"/> ${fn:toUpperCase(marker.markerType.displayName)} PAGES
    </c:set>
</c:if>

<zfin2:subsection title="${title}"
                  test="${!empty links}">
    <table class="summary">
        <tr>
                <%-- entry.key is the database name--%>
            <c:forEach var="entry" items="${links}">
                <td>
                    <%-- entry.value is the MarkerDBLink --%>
                    <c:forEach var="dblink" items="${entry.value}">
                        <li>
                            <zfin:link entity="${dblink}"/>
                            <zfin:attribution entity="${dblink}"/>
                        </li>
                    </c:forEach>
                </td>
            </c:forEach>
        </tr>
    </table>
</zfin2:subsection>
