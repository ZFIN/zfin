<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page title="ZFIN: Bad Result Ticket: ${dynamicTitle}">
    <span class="error">Requested result not found: ${formBean.ticketNumber}</span>

    Please try your <a href="/action/blast/blast">BLAST again</a>.
</z:page>