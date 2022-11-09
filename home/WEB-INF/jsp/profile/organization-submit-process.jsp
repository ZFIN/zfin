<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">

    <div class="container">
        <h1>Register for ZFIN Account</h1>

        <c:if test="${error}">
            <div class="alert alert-danger">
                Error encountered while creating account. Please try again or contact us at zfinadmn@zfin.org.
            </div>
        </c:if>

        <p>Thank you for your submission. We will get back to you as soon as your request is approved.</p>

    </div>

</z:page>