<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<button id="input-welcome-button" rel="#input-welcome-overlay">Your Input Welcome</button>

<div class="jq-modal" id="input-welcome-overlay">
    <div class="popup-content">
        <div class="popup-header">
            Your Input Welcome
        </div>
        <div class="popup-body" id="input-welcome-body">
            <form id="input-welcome-form">
                We welcome your input and comments. Please use this form to recommend updates to the information in ZFIN. We
                appreciate as much detail as possible and references as appropriate. We will review your comments promptly.
                <div id="input-welcome-form-controls">
                    <div id="input-welcome-validate">
                        Please check the highlighted fields and try again.
                    </div>
                    <div class="control">
                        <label for="input-welcome-name">Name:</label>
                        <input type="text" id="input-welcome-name" name="yiw-name" />
                    </div>
                    <div class="control">
                        <label for="input-welcome-institution">Institution:</label>
                        <input type="text" id="input-welcome-institution" name="yiw-institution" />
                    </div>
                    <div class="control">
                        <label for="input-welcome-email">Email address:</label>
                        <input type="text" id="input-welcome-email" name="yiw-email" />
                    </div>
                    <%-- spam prevention technique. input will be hidden from user by JS. controller will consider
                         the request as genuine only if the field is blank. --%>
                    <div class="control" id="input-welcome-email2-ctrl">
                        <label for="input-welcome-email2">Please leave blank:</label>
                        <input type="text" id="input-welcome-email2" name="email" autocomplete="off"/>
                    </div>
                    <div class="control">
                        <label for="input-welcome-subject">Subject:</label>
                        <input type="text" id="input-welcome-subject" name="yiw-subject" />
                    </div>
                    <div class="control">
                        <label for="input-welcome-comments">Comments:</label>
                        <textarea id="input-welcome-comments" name="yiw-comments"></textarea>
                    </div>
                    <div class="control">
                        <button type="submit">Send your comments</button>
                    </div>
                </div>
            </form>
            <div id="input-welcome-success-message">
                Thank you for submitting comments. Your input has been emailed to ZFIN curators who may contact you if
                additional information is required.
            </div>
            <div id="input-welcome-error-message">
                Oops. Something went wrong. Please try again later.
            </div>
        </div>
    </div>
</div>

