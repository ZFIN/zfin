<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag body-content="scriptless" %>
<%@attribute name="collection" type="java.util.Collection" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@attribute name="showAttributionLinks" type="java.lang.Boolean" required="false" %>
<%@attribute name="numberOfEntities" type="java.lang.Integer" required="false" %>
<%@attribute name="ajaxLink" type="java.lang.String" required="false" %>
<%@attribute name="commaDelimited" type="java.lang.Boolean" required="false" %>

<c:if test="${showAttributionLinks == null}">
    <c:set var="showAttributionLinks" value="false" />
</c:if>

<c:if test="${fn:length(collection) > 0 }">
    <ul class="${commaDelimited ? 'comma-separated' : 'list-unstyled'}" data-toggle="collapse"
        data-show="${maxNumber}" data-count="${numberOfEntities}" data-url="${ajaxLink}">
        <c:forEach var="entity" items="${collection}">
            <li>
                <zfin:link entity="${entity}" /><c:if test="${showAttributionLinks}"> <zfin:attribution entity="${entity}" /></c:if>
            </li>
        </c:forEach>
    </ul>
</c:if>
