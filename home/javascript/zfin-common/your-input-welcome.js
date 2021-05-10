$(function () {
    var $overlay = jQuery("#input-welcome-overlay"),
        $successMessage = jQuery("#input-welcome-success-message"),
        $errorMessage = jQuery("#input-welcome-error-message"),
        $triggerButton = jQuery("#input-welcome-button"),
        $form = jQuery("#input-welcome-form"),
        $formInputs = jQuery(":input", $form),
        $formValidate = jQuery("#input-welcome-validate"),
        url = "/action/user-comment",
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

    // move the overlay up to the body tag so that it doesn't get caught up
    // in other CSS rules
    $overlay.appendTo(jQuery("body"));
    $successMessage.hide();
    $errorMessage.hide();
    $formValidate.hide();
    // hide the spam-preventer input
    jQuery("#input-welcome-email2-ctrl").hide();

    $overlay.on(jQuery.modal.CLOSE, function() {
        $successMessage.hide();
        $errorMessage.hide();
        $form.show();
        $formInputs.attr("disabled", false);
        $formValidate.hide();
        $formInputs.removeClass("invalid");
    });

    $triggerButton.click(function(evt) {
        evt.preventDefault();
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

        jQuery.ajax({
            url: url,
            type: "POST",
            data: $form.serialize(),
            success: function() {
                $form.hide();
                $form[0].reset();
                $successMessage.show();
            },
            error: function() {
                $form.hide();
                $errorMessage.show();
            }
        });
        $formValidate.hide();
        $formInputs.removeClass("invalid");
        $formInputs.attr("disabled", true);
    });
});
