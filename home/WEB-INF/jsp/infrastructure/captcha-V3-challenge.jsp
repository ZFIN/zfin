<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="recaptchaSiteKey" value="${siteKey}" />
<c:set var="recaptchaRedirect" value="${redirect}" />

<z:emptyPage omitZfinCommonJS="true">

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div style="display: none;" class="captcha-challenge p-4 border rounded shadow">
                    <h3>zfin.org</h3>
                    <p>Running verification to filter robot traffic.</p>

                    <div class="border text-left m-3 p-3" style="background-color: #eee;">
                        <i class="fas fa-spinner fa-spin" id="loading"></i> Verifying...
                    </div>

                    <p>Our site is experiencing large amounts of traffic right now.
                        To ensure our community of researchers get the best experience, we need to make sure you are human.</p>

                    <p>Please let us know if you experience any problems while we work to address this problem.<br/>
                        <a href="https://wiki.zfin.org/display/general/ZFIN+Contact+Information">Contact Information</a>.
                    </p>

                    <form id="captcha-form" method="post">
                        <input type="hidden" name="redirect" value="${recaptchaRedirect}"/>
                        <input type="hidden" id="recaptchaResponse" name="g-recaptcha-response" value=""/>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://www.google.com/recaptcha/api.js?render=${recaptchaSiteKey}"></script>
    <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

    <script>
        // Ensure the body is visible after the CSS has been loaded
        window.onload = function() {
            const div = document.querySelector(".captcha-challenge");
            div.style.display = 'block';
            redirectIn2Seconds();
        };
        function submitRecaptchaResponseToken() {
            grecaptcha.ready(function() {
                grecaptcha.execute('${recaptchaSiteKey}', {action: 'submit'}).then(function(token) {
                    document.getElementById('recaptchaResponse').value = token;
                    document.getElementById('captcha-form').submit();
                });
            });
        }
        function redirectIn2Seconds() {
            setTimeout(submitRecaptchaResponseToken, 2000);
        }
    </script>

</z:emptyPage>