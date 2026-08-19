<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">

    <div class="container">
        <h1>Register for ZFIN Account</h1>

        <c:choose>
            <c:when test="${submissionRejected or error}">
                <div class="alert alert-danger" role="alert">
                    <h4 class="alert-heading">Your request was NOT submitted successfully.</h4>
                    <p class="mb-0">
                        Please follow up using the feedback form at
                        <a href="https://zfin.org" class="alert-link">zfin.org</a>.
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <p>Thank you for your submission. We will get back to you as soon as your request is approved.</p>
            </c:otherwise>
        </c:choose>

    </div>

</z:page>
