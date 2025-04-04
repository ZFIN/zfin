<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:emptyPage>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div style="display: none;" class="captcha-challenge text-center p-4 border rounded shadow">
                    <h2>Robot Check</h2>
                    <p>Validation failed</p>
                </div>
            </div>
        </div>
    </div>

    <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">
    <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
    <script src="${zfn:getAssetPath("vendor-common.js")}"></script>
    <script src="${zfn:getAssetPath("zfin-common.js")}"></script>
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
    <script src="${zfn:getAssetPath("bootstrap.js")}"></script>

    <script>
        // Ensure the body is visible after the CSS has been loaded
        window.onload = function() {
            const div = document.querySelector(".captcha-challenge");
            div.style.display = 'block';
        };
    </script>
</z:emptyPage>