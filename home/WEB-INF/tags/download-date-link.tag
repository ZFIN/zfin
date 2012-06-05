<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="date" type="java.lang.String" required="true" %>

<a href="/action/unload/downloads/archive/${date}">${date}</a>