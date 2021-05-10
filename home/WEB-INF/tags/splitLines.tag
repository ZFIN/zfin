<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="input" type="java.lang.String"%>

<c:set var="newLine" value="
"/>

${fn:replace(fn:trim(input), newLine , "<br>")}


