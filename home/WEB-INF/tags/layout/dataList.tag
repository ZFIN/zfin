<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="collapse" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<z:ifHasData test="${hasData}">
    <div class="data-list-container">
        <ul>
            <jsp:doBody />
        </ul>
    </div>
</z:ifHasData>
