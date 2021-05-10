<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<select id="select">
    <c:forEach var="aPrefix" items="${prefixes}">
        <option
            ${aPrefix eq prefix ? "selected": ""}
                >${aPrefix}</option>
    </c:forEach>
</select>
<input type="button" id="saveButton" value="Save Prefix"/>



