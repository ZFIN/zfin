<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="items" type="java.util.Collection" required="true" %>
<%@ attribute name="id" type="java.lang.String" required="true" %>
<%@ attribute name="itemName" type="java.lang.String" required="true" %>

<c:set var="shortVal" value="short-${id}" scope="request"/>
<c:set var="longVal" value="long-${id}" scope="request"/>

<div id="${shortVal}" style="display: inline;">
    <c:forEach var="term" items="${items}" end="4">
    <span class="related-ontology-term" id="${term.entityName}">
       <zfin:link entity="${term}"/>
    </span>
    </c:forEach>
    <c:if test="${fn:length(items) > 5}">
        <a href="javascript:toggle('${shortVal}', '${longVal}')" style="font-size: 12px">
            ... <img src="<c:url value="/images/plus-symbol.png" />" alt="expand"/> Show
            All ${fn:length(items)} ${itemName}
        </a>
    </c:if>
</div>

<div id="${longVal}" style="display: none;">
    <c:forEach var="term" items="${items}">
    <span class="related-ontology-term" id="${term.entityName}">
       <zfin:link entity="${term}"/>
    </span>
    </c:forEach>
    <a href="javascript:toggle( '${longVal}','${shortVal}')" style="font-size: 12px">
        <img src="<c:url value="/images/minus-symbol.png" />" alt="collapse"/> Show first 5 ${itemName}
    </a>
</div>

<script type="text/javascript">
    function toggle(shortVal, longVal) {
        document.getElementById(shortVal).style.display = 'none';
        document.getElementById(longVal).style.display = 'inline';
    }
</script>
