<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="captchaRedirect" value="${redirect}" />

<z:emptyPage omitZfinCommonJS="true">

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div style="display: none;" class="captcha-challenge p-4 border rounded shadow">
                    <div class="text-center mb-3">
                        <a href="/"><img src="/images/zfinlogo_lg.gif" alt="ZFIN" style="max-height: 48px;"></a>
                    </div>
                    <h3>Verify you're human</h3>
                    <p>ZFIN is experiencing heavy traffic right now. To keep the site fast for our community of researchers, we need a quick check to confirm you're not a bot.</p>

                    <p class="small text-muted">
                        Having trouble? <a href="https://wiki.zfin.org/display/general/ZFIN+Contact+Information">Let us know</a>.
                    </p>

                    <form id="captcha-form" method="post" action="/action/captcha/challenge">
                        <input type="hidden" name="redirect" value="${fn:escapeXml(captchaRedirect)}"/>
                        <altcha-widget
                                challengeurl="/action/altcha/challenge"
                        ></altcha-widget>
                        <div class="mb-3 mt-3">
                            <button type="submit" id="captcha-submit" class="btn btn-primary" disabled>Submit</button>
                        </div>
                    </form>

                    <hr/>
                    <c:url var="loginUrl" value="/action/login">
                        <c:if test="${not empty captchaRedirect}">
                            <c:param name="redirect" value="${captchaRedirect}"/>
                        </c:if>
                    </c:url>
                    <p class="mb-0 small text-muted">Have a ZFIN account? <a href="${loginUrl}">Sign in</a> to skip the verification.</p>
                </div>
            </div>
        </div>
    </div>

    <script async defer src="https://cdn.jsdelivr.net/npm/altcha/dist/altcha.min.js" type="module"></script>

    <link rel="stylesheet" href="${zfn:getAssetPath("style.css")}">
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

    <script>
        window.onload = function() {
            document.querySelector(".captcha-challenge").style.display = 'block';

            const widget = document.querySelector('altcha-widget');
            const submitBtn = document.getElementById('captcha-submit');
            if (widget && submitBtn) {
                widget.addEventListener('statechange', (ev) => {
                    submitBtn.disabled = ev.detail.state !== 'verified';
                });
                widget.addEventListener('expired', () => {
                    submitBtn.disabled = true;
                });
            }
        };
    </script>

</z:emptyPage>