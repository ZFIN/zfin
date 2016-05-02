<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize access="hasRole('root')">
    <ul>
        <li><a href='/action/profile/lab/all-labs'>List all labs</a> </li>
        <li><a href='/action/profile/company/all-companies'>List all companies</a> </li>
        <li><a href='/action/profile/person/all-people/A'>List all people</a> </li>
    </ul>
</authz:authorize>

