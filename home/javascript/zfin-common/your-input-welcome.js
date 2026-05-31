$(function () {
    var $overlay = jQuery("#input-welcome-overlay"),
        $successMessage = jQuery("#input-welcome-success-message"),
        $errorMessage = jQuery("#input-welcome-error-message"),
        $triggerButton = jQuery("#input-welcome-button"),
        $form = jQuery("#input-welcome-form"),
        $formInputs = jQuery(":input", $form),
        $submitButton = jQuery("#input-welcome-submit", $form),
        $formValidate = jQuery("#input-welcome-validate"),
        $captcha = jQuery("#input-welcome-captcha"),
        $captchaMessage = jQuery("#input-welcome-captcha-message"),
        captchaWidget = document.querySelector("#altcha-widget"),
        // whether the server says this visitor must pass a captcha, and whether they've done so
        captchaRequired = false,
        captchaVerified = false,
        url = "/action/user-comment",
        captchaCheckUrl = "/action/captcha/required",
        stringNotEmpty = function (str) {
            return str.length > 0 && str.trim();
        },
        validEmail = function(str) {
            return /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]+$/i.test(str);
        },
        validators = {
            "#input-welcome-name": stringNotEmpty,
            "#input-welcome-institution": stringNotEmpty,
            "#input-welcome-email": validEmail,
            "#input-welcome-subject": stringNotEmpty,
            "#input-welcome-comments": stringNotEmpty
        };

    // the submit button is enabled unless we're still waiting on a required captcha
    function updateSubmitEnabled() {
        $submitButton.attr("disabled", captchaRequired && !captchaVerified);
    }

    function showCaptcha(withMessage) {
        captchaRequired = true;
        $captcha.show();
        $captchaMessage.toggle(!!withMessage);
        updateSubmitEnabled();
    }

    function hideCaptcha() {
        captchaRequired = false;
        $captcha.hide();
        $captchaMessage.hide();
        updateSubmitEnabled();
    }

    // force a fresh challenge so a stale/expired solution isn't reused
    function resetCaptcha() {
        captchaVerified = false;
        if (captchaWidget && typeof captchaWidget.reset === "function") {
            captchaWidget.reset();
        }
    }

    if (captchaWidget) {
        captchaWidget.addEventListener("statechange", function (ev) {
            if (ev.detail.state === "verified") {
                captchaVerified = true;
                updateSubmitEnabled();
            }
        });
    }

    // move the overlay up to the body tag so that it doesn't get caught up
    // in other CSS rules
    $overlay.appendTo(jQuery("body"));
    $successMessage.hide();
    $errorMessage.hide();
    $formValidate.hide();
    $captcha.hide();
    $captchaMessage.hide();
    // hide the spam-preventer input
    jQuery("#input-welcome-email2-ctrl").hide();

    $overlay.on(jQuery.modal.CLOSE, function() {
        $successMessage.hide();
        $errorMessage.hide();
        $form.show();
        $formInputs.attr("disabled", false);
        $formValidate.hide();
        $captchaMessage.hide();
        $formInputs.removeClass("invalid");
        updateSubmitEnabled();
    });

    $triggerButton.click(function(evt) {
        evt.preventDefault();
        // Ask the server whether this visitor needs to pass a captcha (logged-in or already
        // verified users don't). Default to not requiring it if the check fails; the server
        // still enforces captcha on submit and we recover gracefully in the error handler.
        jQuery.ajax({
            url: captchaCheckUrl,
            type: "GET",
            dataType: "json",
            cache: false, // captcha requirement changes with login/session state; never serve a stale answer
            success: function(data) {
                if (data && data.required) {
                    showCaptcha(false);
                } else {
                    hideCaptcha();
                }
            },
            error: function() {
                hideCaptcha();
            }
        });
        $overlay.modal({
            fadeDuration: 100
        });
    });

    $form.submit(function(evt) {
        evt.preventDefault();

        var valid = true,
            $input;

        $formInputs.removeClass("invalid");
        for (var selector in validators) {
            if (validators.hasOwnProperty(selector)) {
                $input = jQuery(selector);
                if (!validators[selector]($input.val())) {
                    valid = false;
                    $input.addClass("invalid")
                }
            }
        }
        if (!valid) {
            $formValidate.show();
            return;
        }

        $formValidate.hide();
        $captchaMessage.hide();
        $formInputs.removeClass("invalid");

        // Serialize BEFORE disabling inputs: jQuery's .serialize() skips disabled fields,
        // so disabling first would drop every value (yiw-name, etc.) from the request.
        var formData = $form.serialize();
        $formInputs.attr("disabled", true);

        jQuery.ajax({
            url: url,
            type: "POST",
            data: formData,
            success: function() {
                $form.hide();
                $form[0].reset();
                $successMessage.show();
                captchaVerified = false;
                updateSubmitEnabled();
            },
            error: function(jqXHR) {
                // If the captcha became required since the form was opened (e.g. the session
                // expired), surface the widget and let the user resend without losing their
                // comment, instead of showing a generic error.
                var response = jqXHR.responseJSON;
                if (response && response.status === "CaptchaRequired") {
                    $formInputs.attr("disabled", false);
                    resetCaptcha();
                    showCaptcha(true);
                } else {
                    $form.hide();
                    $errorMessage.show();
                }
            }
        });
    });
});
