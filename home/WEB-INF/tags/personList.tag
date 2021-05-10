<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="letter" type="java.lang.String" required="false" %>
<%@ attribute name="people" type="java.util.Collection" required="true" %>

<c:if test="${!empty letter}">
    <c:set var="alphabet" value="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z"/>

    <div style='background-color: #d3d3d3;' align="center">
        Last name starts with:
        <c:forTokens items="${alphabet}" var="letter" delims="," varStatus="status">
            <a href='/action/profile/person/all-people/${letter}'>${letter}</a>
            ${!status.last ? '&#149;':''}
        </c:forTokens>
    </div>
</c:if>

<br/>


<c:if test="${!empty people}">
    <table class="searchresults rowstripes">
        <tr>
            <th width="45%">
                Name
            </th>
            <th>

            </th>
            <th>
                Address
            </th>
        </tr>
        <c:forEach var="person" items="${people}" varStatus="loop">
            <tr class=${loop.index%2==0 ? "even" : "odd"}>
                <td>
                    <zfin:link entity="${person}"/>
                    <authz:authorize access="hasRole('root')">
                        ${person.accountInfo.login}
                        ${person.accountInfo.role eq 'root' ? '<div class=error-inline>root</div>' : ''}
                    </authz:authorize>
                </td>
                <td>
                    <authz:authorize access="hasRole('root')">
                        <a class="small-new-link" href='/action/profile/person/edit/${person.zdbID}'>Edit</a>
                    </authz:authorize>
                </td>
                <td>
                    <c:if test="${!empty person.email}">
                        <div><a href="mailto:${person.email}">${person.email}</a></div>
                    </c:if>
                    <c:if test="${!empty person.address}">
                        <div class="postal-address">${person.address}</div>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </table>

</c:if>

