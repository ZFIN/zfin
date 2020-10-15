<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
    <script src="${zfn:getAssetPath("bootstrap.js")}"></script>

    <div class='container-fluid'>
        <div class="alert alert-danger" role="alert">
            An error occurred! ${error}
            Please contact <a href="mailto:zfinadmn@zfin.org?subject=Exception&body=${pageURL}">zfinadmn@zfin.org</a>
        </div>

        <c:if test="${not empty exception}">
            <div>
                <p><strong>${exception}</strong></p>
            </div>
            <p>Stack Trace:</p>
            <pre>${stackTrace}</pre>
        </c:if>
    </div>
</z:page>
