<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style type="text/css">
    div.summary li { padding-top: .1em; padding-bottom: .1em; }
</style>

<div class="popup-header">
    Transcript Types
</div>
<div class="popup-body">
    <ul>
        <c:forEach var="type" items="${formBean.transcriptTypeList}">
            <c:choose>
                <c:when test="${type.indented}">
                    <ul><li><a name="${type.type}"/> <b>${type.display}</b> - ${type.definition}</li></ul>
                </c:when>
                <c:otherwise>
                    <li><a name="${type.type}"/> <b>${type.display}</b> - ${type.definition}</li>
                </c:otherwise>
            </c:choose>
        </c:forEach>

    </ul>
</div>


