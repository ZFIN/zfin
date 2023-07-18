<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="queryString" required="true" type="java.lang.String" %>
<%@attribute name="baseUrlWithoutPage" required="true" type="java.lang.String" %>


<div>

    <div id="facet-list-controller">
        <div
                class="__react-root __use-event-bus"
                id="QuickSearchDialog"
                data-base-url-without-page="${baseUrlWithoutPage}"
                data-query-string="${queryString}"
        ></div>
    </div>

</div>


