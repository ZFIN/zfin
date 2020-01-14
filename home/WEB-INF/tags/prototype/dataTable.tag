<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="collapse" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<zfin-prototype:ifHasData test="${hasData}">
    <div class="data-table-container">
        <table class="data-table" data-table="${collapse ? 'collapse' : ''}" data-toggle-container=".data-table-pagination">
            <jsp:doBody />
        </table>
        <c:if test="${collapse}">
            <div class="data-table-pagination">
            </div>
        </c:if>
    </div>
</zfin-prototype:ifHasData>
