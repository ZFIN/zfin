<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="collapse" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<z:ifHasData test="${hasData}">
    <div class="data-table-container">
        <div class="horizontal-scroll-container">
            <table class="data-table" data-table="${collapse ? 'collapse' : ''}" data-toggle-container=".data-pagination-container">
                <jsp:doBody />
            </table>
        </div>
        <div class="data-pagination-container"></div>
    </div>
</z:ifHasData>
