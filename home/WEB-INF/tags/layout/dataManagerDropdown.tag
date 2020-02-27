<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize access="hasRole('root')">
    <div class="dropdown float-right">
        <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-toggle="dropdown">
            <i class="fas fa-cog"></i>
        </button>
        <div class="dropdown-menu dropdown-menu-right">
            <jsp:doBody />
        </div>
    </div>
</authz:authorize>