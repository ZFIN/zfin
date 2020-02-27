<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="label" required="true" rtexprvalue="true" %>

<dt class="col-sm-2">${label}</dt>
<dd class="col-sm-10"><jsp:doBody /></dd>
