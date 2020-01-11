<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<zfin-prototype:ifHasData test="${hasData}">
    <div class="data-table-container">
        <table class="data-table">
            <jsp:doBody />
        </table>
    </div>
</zfin-prototype:ifHasData>
