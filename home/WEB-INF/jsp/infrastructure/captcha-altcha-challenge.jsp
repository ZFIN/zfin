<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="captchaRedirect" value="${redirect}" />

<z:emptyPage omitZfinCommonJS="true">

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div style="display: none;" class="captcha-challenge p-4 border rounded shadow">
                    <h3>zfin.org</h3>
                    <p>Running verification to filter robot traffic.</p>

                    <p>Our site is experiencing large amounts of traffic right now.
                        To ensure our community of researchers get the best experience, we need to make sure you are human.</p>

                    <p>Please let us know if you experience any problems while we work to address this problem.<br/>
                        <a href="https://wiki.zfin.org/display/general/ZFIN+Contact+Information">Contact Information</a>.
                    </p>

                    <form id="captcha-form" method="post" action="/action/captcha/challenge">
                        <input type="hidden" name="redirect" value="${captchaRedirect}"/>
                        <altcha-widget
                                challengeurl="/action/altcha/challenge"
                        ></altcha-widget>
                        <div class="mb-3 mt-3">
                            <button type="submit" class="btn btn-primary">Submit</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script async defer src="https://cdn.jsdelivr.net/npm/altcha/dist/altcha.min.js" type="module"></script>

    <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

    <script>
        // Ensure the body is visible after the CSS has been loaded
        window.onload = function() {
            const div = document.querySelector(".captcha-challenge");
            div.style.display = 'block';
        };
    </script>

</z:emptyPage>