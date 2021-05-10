<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<div class="row">
    <div class="col-lg-6 offset-lg-3 feedback-box alert" style="display: none; margin-top: 1em;">
        <a href="#" class="close" data-hide="feedback-box">&times;</a>
        <form style="margin-top: 18px; clear:both;" class="your-input-welcome-form form-horizontal" name="your-input-welcome" id="feedback-form">

            <div class="form-group row">
                <label for="feedbackName" class="col-form-label col-2">Name</label>
                <div class="col-10">
                    <input name="yiw-name" id="feedbackName" type="text" class="form-control required"/>
                </div>
            </div>
            <div class="form-group row">
                <label for="feedbackInstitution" class="col-form-label col-2">Institution</label>
                <div class="col-10">
                    <input name="yiw-institution" id="feedbackInstitution" type="text" class="form-control required"/>
                </div>
            </div>
            <div class="form-group row">
                <label for="feedbackEmail" class="col-form-label col-2">Email</label>
                <div class="col-10">
                    <input name="yiw-email" id="feedbackEmail" class="form-control required email" type="text"/>
                </div>
            </div>
            <%-- spam preventer --%>
            <div class="form-group row" style="display: none">
                <label for="feedbackEmail2" class="col-form-label col-2">Please leave blank</label>
                <div class="col-10">
                    <input type="text" id="feedbackEmail2" name="email" autocomplete="off" class="form-control"/>
                </div>
            </div>
            <div class="form-group row">
                <label for="feedbackSubject"class="col-form-label col-2">Subject</label>
                <div class="col-10">
                    <input id="feedbackSubject" type=text name="yiw-subject" class="required form-control"/>
                </div>
            </div>
            <div class="form-group row">
                <label for="feedbackComments" class="col-form-label col-2">Comments</label>
                <div class="col-10">
                    <textarea class="form-control required" id="feedbackComments" name="yiw-comments" rows=7></textarea>
                </div>
            </div>
            <div class="form-group row">
                <div class="offset-2 col-10">
                    <input type="submit" value="Send" class="btn btn-primary" id="feedback-send-button"/>
                    <button class="btn btn-outline-secondary" data-hide="feedback-box">Close</button>
                </div>
            </div>
            <div class="offset-2 col-10">
                <small>Screenshots can be sent to <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a></small>
            </div>
        </form>
    </div>

    <div id="feedback-thanks-message" class="offset-lg-3 col-lg-6 alert alert-success feedback-done" style="display: none;">
        <a href="#" class="close" data-hide="alert">&times;</a>
        Thanks for your feedback!
    </div>

    <div id="feedback-error-message" class="offset-lg-3 col-lg-6 alert alert-error feedback-done" style="display: none;">
        <a href="#" class="close" data-hide="alert">&times;</a>
        <strong>Oh no!</strong>
        Something went wrong on our end. Please email your feedback
        <a href="mailto:zfinadmn@zfin.org">directly to us</a> or try again later.
    </div>
</div>


<script>
    jQuery(function () {
        jQuery(".feedback-link").click( function (evt) {
            evt.preventDefault();
            jQuery(".feedback-done").hide();
            jQuery(".feedback-box").slideToggle(50);
        });

        jQuery("[data-hide]").on("click", function (evt) {
            evt.preventDefault();
            jQuery(this).closest("." + jQuery(this).data("hide")).hide();
        });

        jQuery('#feedback-form').validate({
            submitHandler: function (form) {
                jQuery.ajax({
                    url: '/action/user-comment',
                    type: 'POST',
                    data: jQuery(form).serialize(),
                    success: function () {
                        jQuery(".feedback-box").hide();
                        jQuery('#feedback-thanks-message').show();
                        form.reset();
                    },
                    error: function ()  {
                        jQuery(".feedback-box").hide();
                        jQuery('#feedback-error-message').show();
                    }
                });
            }
        });
    });
</script>
