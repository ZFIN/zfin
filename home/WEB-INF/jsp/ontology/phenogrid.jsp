<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

Back to <zfin:link entity="${term}"/>

<zfin-ontology:phenogrid doid="${term.oboID}"/>
