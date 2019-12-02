<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<zfin-prototype:ifHasData test="${hasData}">
    <table class="table table-hover">
        <jsp:doBody />
    </table>
</zfin-prototype:ifHasData>
