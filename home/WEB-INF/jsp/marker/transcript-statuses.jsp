<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style type="text/css">
    div.summary li { padding-top: .1em; padding-bottom: .1em; }
</style>

<div class="popup-header">
    Annotation Status
</div>
<div class="popup-body">
    <ul>
        <c:forEach var="typeStatus" items="${formBean.transcriptTypeStatusDefinitionList}">
            <li> <b> ${typeStatus.status.display}  ${typeStatus.type.display} </b> - ${typeStatus.definition} </li>
        </c:forEach>
    </ul>
</div>



