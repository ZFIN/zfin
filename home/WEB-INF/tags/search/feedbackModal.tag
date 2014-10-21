<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="span8 offset2 secondary-action-box feedback-box well" style="display: none;">
    <%-- I'm sure the relative positioning I'm doing here is a side effect of something else dumb I've done... --%>
    <a href="#" class="close" data-dismiss="alert" style="position:relative; top: -3px; left:0px">&times;</a>

    <div>

        <form class="your-input-welcome-form" NAME="your-input-welcome" id="feedback-form"
              METHOD="post"
              ACTION="/cgi-bin/input_welcome_generic.cgi">


            <div class="your-input-requied-box">


                <div>
                    <input NAME="firstname" type="text" class="input-large required" placeholder="First Name"/>
                    <input NAME="lastname" type="text" class="input-large required" placeholder="Last Name"/>
                    <input NAME="institution" type="text" class="input-xxlarge required" placeholder="Institution"/>
                    <input id="feedback-email-input" class="input-xxlarge required email" type="text" name="email"
                           placeholder="Email Address">

                </div>
                <div>

                    <INPUT class="input-xxlarge required" TYPE=text NAME="subject" SIZE=45 placeholder="Subject">
                    <input TYPE=hidden NAME="page_name"
                           VALUE="http://<%=ZfinPropertiesEnum.INSTANCE%>.zfin.org${baseUrl}">
                </div>
                <div>

                    <TEXTAREA class="input-xxlarge required" NAME="comments"
                              ROWS=7 <%--placeholder="Wow, great work!"--%>></TEXTAREA>
                </div>


            </div>
            <div>
                <input type="submit" value="Send" class="btn btn-primary" id="feedback-send-button"/>
                <button class="btn" onclick="jQuery('.secondary-action-box').hide();" aria-hidden="true">Close</button>
                <span style="padding-left: 3.5em;">send screenshots to <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a></span>

            </div>

        </FORM>

    </div>



</div>


<div id="feedback-thanks-message" class="row" style="display:none">
    <div class="offset3 span6 alert alert-success">
        <a href="#" class="close" data-dismiss="alert" onclick="jQuery('#feedback-thanks-message').hide();">&times;</a>
        Thanks for your feedback!
    </div>
</div>

<div id="feedback-error-message" class="row" style="display:none">
    <div class="offset3 span6 alert alert-error">
        <a href="#" class="close" data-dismiss="alert" onclick="jQuery('#feedback-error-message').hide();">&times;</a>
        <b>Something went wrong!</b>

        <div style="margin-top:.5em;">Since our feedback script doesn't seem to be working,
            try sending a direct email to <a href="mailto:cases@zfinlabs.fogbugz.com">cases@zfinlabs.fogbugz.com</a>
        </div>
    </div>
</div>