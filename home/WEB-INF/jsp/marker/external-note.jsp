<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<style type="text/css">
    div.summary li {
        padding-top: .1em;
        padding-bottom: .1em;
    }
</style>

<div class="popup-header">
    Orthology Note for <zfin:link entity="${marker}"/>
</div>

<div class="popup-body">
    <c:forEach var="note" items="${notes}">
        <%--<b>Note:</b>--%>
        <p>
                ${note}
        </p>
    </c:forEach>
</div>
