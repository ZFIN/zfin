<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="earliestStartStage" type="org.zfin.anatomy.DevelopmentStage" required="true" %>
<%@ attribute name="latestEndStage" type="org.zfin.anatomy.DevelopmentStage" required="true" %>

<zfin:link entity="${earliestStartStage}"/>
<c:if test="${earliestStartStage != latestEndStage}">
    to <zfin:link entity="${latestEndStage}"/>
</c:if>
