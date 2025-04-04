<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="recaptchaSiteKey" value="${siteKey}" />
<c:set var="recaptchaRedirect" value="${redirect}" />

<z:emptyPage>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div style="display: none;" class="captcha-challenge text-center p-4 border rounded shadow">
                    <h2>Robot Check</h2>
                    <p>We need to make sure you are not a robot.</p>
                    <form id="captcha-form" method="post">
                        <input type="hidden" name="redirect" value="${recaptchaRedirect}"/>
                        <div class="mb-3"></div>
                        <div style="display: inline-block;" class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>
                        <div class="mb-3">
                            <button type="submit" class="btn btn-primary">Submit</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://www.google.com/recaptcha/api.js"></script>
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
        function onSubmit(token) {
            document.getElementById("captcha-form").submit();
        }
    </script>

</z:emptyPage>