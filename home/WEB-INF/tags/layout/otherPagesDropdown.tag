<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="btn-group">
    <button
        class="btn btn-sm btn-link p-0"
        type="button"
        data-toggle="dropdown"
        data-boundary="window"                      <%-- these settings prevent the popup from being cut off --%>
        data-popper-config="{positionFixed: true}"  <%-- by the table horizontal scrolling container --%>
    >
        <i class="fas fa-link"></i>
    </button>
    <div class="dropdown-menu">
        <jsp:doBody />
    </div>
</div>