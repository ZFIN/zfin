<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<div class="alert alert-danger" role="alert">
    An error occurred! ${error}
</div>

<div>
    <p><strong>${exception}</strong></p>
</div>
<p>Stack Trace:</p>
<p>
    <small>${stackTrace}</small>
</p>
</div>

