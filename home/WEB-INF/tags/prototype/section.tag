<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="title" required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<section class="section" id="${zfn:makeDomIdentifier(title)}">
    <div class="heading">${title}</div>
    <zfin-prototype:ifHasData test="${hasData}">
        <jsp:doBody />
    </zfin-prototype:ifHasData>
</section>
