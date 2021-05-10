<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="value" type="java.lang.Object" %>

<span>${value == null ? 'Unknown' : ( value ? 'Yes' : 'No' )}</span>