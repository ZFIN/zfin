<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="bindExeption" type="org.springframework.validation.BindException" %>


    <ul>
        <c:forEach var="error" items="${bindExeption.allErrors}">
            <li><span class="error">${error.defaultMessage}</span></li>
        </c:forEach>
    </ul>

