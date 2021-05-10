<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="type" type="java.lang.String" required="true" %>
<%@ attribute name="organizations" type="java.util.Collection" required="true" %>

<c:if test="${!empty organizations}">
    <c:forEach var="org" items="${organizations}" varStatus="loop">
        <div style="font-size: small;">
        <b>${org.name}</b>
        <c:if test="${!empty org.address.institution}">
            ${org.address.institution}
        </c:if>
        <c:if test="${!empty org.address.street1}">
            ${org.address.street1}
        </c:if>
        <c:if test="${!empty org.address.street2}">
            ${org.address.street2}
        </c:if>
        ${org.address.city}, ${org.address.stateCode} ${org.address.postalCode}
        ${org.address.countryCode}

        ${org.phone}
        ${org.fax}
        ${org.email}
        ${org.url}
        </div>
        <br/>
    </c:forEach>

</c:if>


