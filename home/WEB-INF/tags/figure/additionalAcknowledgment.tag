<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="publication" type="org.zfin.publication.Publication"%>
<%@attribute name="hasAcknowledgment" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<c:if test="${hasAcknowledgment}">
    <div style="margin-top:1em;">
            ${publication.acknowledgment}
    </div>
</c:if>