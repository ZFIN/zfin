<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="type" type="java.lang.String" required="true" %>
<%@ attribute name="organizations" type="java.util.Collection" required="true" %>

<c:if test="${!empty organizations}">
    <table class="searchresults rowstripes" style="clear: both;">
        <tr>
            <th width="45%">
                    ${type eq 'LAB' ? 'Lab' : 'Company'}
            </th>
            <th>

            </th>
            <th>
                Address
            </th>
        </tr>
        <c:forEach var="org" items="${organizations}" varStatus="loop">
            <tr class=${loop.index%2==0 ? "even" : "odd"}>
                <td><zfin:link entity="${org}"/></td>
                <td>
                    <authz:authorize access="hasRole('root')">
                        <a class="small-new-link"
                           href='/action/profile/${fn:toLowerCase(type)}/edit/${org.zdbID}'>Edit
                        </a>
                    </authz:authorize>
                </td>
                <td class="postal-address">${org.address}</td>
            </tr>
        </c:forEach>
    </table>

</c:if>


