<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize access="hasRole('root')">
    <div class="dropdown float-right">
        <jsp:doBody/>
    </div>
</authz:authorize>