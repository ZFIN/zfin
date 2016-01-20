<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ attribute name="links" type="java.util.HashSet"
              rtexprvalue="true" required="true" %>
<%@ attribute name="feature" type="org.zfin.feature.Feature" rtexprvalue="true" required="true" %>
<%@ attribute name="title" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="showPubs" type="java.lang.String" rtexprvalue="true" required="false" %>
<%-- in the app page, the html for this is a table with list items floating on their
     own, one per cell.  it's kind of odd.  Maybe it can actually be a list, just inline
     and not allowing any wrapping --%>

<c:if test="${empty title}">
    <c:set var="title">
        OTHER&nbsp;<zfin:abbrev entity="${feature}"/> PAGES
    </c:set>
</c:if>


<zfin2:subsection title="${title}" anchor="other_pages"
                  test="${!empty links}" showNoData="true" noDataText="No links to external sites">
    <table class="summary">
        <tr>
            <%--<c:forEach var="link" items="${links}" varStatus="loop">--%>
            <%--${(!loop.first && loop.index%3==0 ? "<td>" : "")}--%>
            <td>
                <c:set var="lastRefDB" value=""/>
                <c:forEach var="link" items="${links}" varStatus="loop">
                    <c:set var="refDB" value="${link.referenceDatabase}"/>
                    ${(!loop.first and (refDB ne lastRefDB) ? "</td>" : "")}
                    ${(!loop.first and (refDB ne lastRefDB) ? "<td>" : "")}
                    <%-- entry.value is the MarkerDBLink --%>
                    <li style="list-style-type:none;">
                        <zfin:link entity="${link}"/>
                        <c:if test="${link.publicationCount > 0}">
                            <c:choose>
                                <c:when test="${link.publicationCount == 1}">
                                    (<a href="/${link.singlePublication.zdbID}">${link.publicationCount}</a>)
                                </c:when>
                                <c:otherwise>
                                   <%--could not get tag to allow this syntax to eliminate cgi-bin hardcoding (does not like the %'s/scriplets):
                                    (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${featureSummaryDblink.zdbID}&rtype=genotype">${featureSummaryDblink.publicationCount}</a>)
                                    code review suggestions wildly accepted--%>
                                 (<a href="/cgi-bin/webdriver?MIval=aa-showpubs.apg&OID=${link.zdbID}&rtype=genotype">${link.publicationCount}</a>)--%>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </li>
                    <c:set var="lastRefDB" value="${refDB}"/>
                </c:forEach>
                <%--</td>--%>
                <%--${(!loop.first && loop.index%3==0 ? "</td>" : "")}--%>
        </tr>
    </table>
</zfin2:subsection>
