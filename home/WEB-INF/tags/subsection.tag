<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag body-content="scriptless" %>

<%@ attribute name="title" required="false" rtexprvalue="true" type="java.lang.String"
              description="If your title contains html, leave this blank
                           and make the title inside the tag" %>
<%@ attribute name="inlineTitle" required="false" rtexprvalue="true" type="java.lang.Boolean"
              description="Set to true if the title should be inline rather than block
                     (no newline between the title and content)" %>
<%@ attribute name="test" required="false" rtexprvalue="true" type="java.lang.Boolean"
              description="Something like ${!empty formBean.collection} should go in here" %>

<c:if test="${empty test || test == true}">
    <c:if test="${((empty inlineTitle) || (inlineTitle == false)) && !empty title}"> </c:if>
    <div class="summary">
        <c:if test="${!empty title}">
            <c:choose>
                <c:when test="${(!empty inlineTitle) && (inlineTitle == true)}">
                    <span class="summaryTitle">${title}:</span>
                </c:when>
                <c:otherwise>                    
                    <div class="summaryTitle">${title}</div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <jsp:doBody/>

    </div>
</c:if>