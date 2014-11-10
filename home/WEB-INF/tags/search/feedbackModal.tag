<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="row-fluid">
    <div class="span6 offset3 feedback-box alert" style="display: none;">
        <a href="#" class="close" data-hide="feedback-box">&times;</a>
        <form class="your-input-welcome-form form-horizontal" name="your-input-welcome" id="feedback-form">
            <fieldset>
                <div class="control-group">
                    <label class="control-label" for="feedbackName">Name</label>
                    <div class="controls">
                        <input name="yiw-name" id="feedbackName" type="text" class="input-block-level required"/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="feedbackInstitution">Institution</label>
                    <div class="controls">
                        <input name="yiw-institution" id="feedbackInstitution" type="text" class="input-block-level required"/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="feedbackEmail">Email</label>
                    <div class="controls">
                        <input name="yiw-email" id="feedbackEmail" class="input-block-level required email" type="text"/>
                    </div>
                </div>
                <%-- spam preventer --%>
                <div class="control-group" style="display: none">
                    <label class="control-label" for="feedbackEmail2">Please leave blank</label>
                    <div class="controls">
                        <input type="text" id="feedbackEmail2" name="email" autocomplete="off"/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="feedbackSubject">Subject</label>
                    <div class="controls">
                        <input class="input-block-level required" id="feedbackSubject" type=text name="yiw-subject"/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="feedbackComments">Comments</label>
                    <div class="controls">
                        <textarea class="input-block-level required" id="feedbackComments" name="yiw-comments" rows=7></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <input type="submit" value="Send" class="btn btn-zfin" id="feedback-send-button"/>
                        <button class="btn" data-hide="feedback-box">Close</button>
                    </div>
                </div>
                <div class="controls">
                    <small>Screenshots can be sent to <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a></small>
                </div>
            </fieldset>
        </form>
    </div>

    <div id="feedback-thanks-message" class="offset3 span6 alert alert-success feedback-done" style="display: none;">
        <a href="#" class="close" data-hide="alert">&times;</a>
        Thanks for your feedback!
    </div>

    <div id="feedback-error-message" class="offset3 span6 alert alert-error feedback-done" style="display: none;">
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
