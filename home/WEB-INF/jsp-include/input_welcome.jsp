<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<form method="post" action="/@WEBDRIVER_LOC@/webdriver" target="comments" style="display:inline;">
    <!---- define Input Welcome button---->
    <input name="subject" value='<tiles:getAsString name="subjectName" />' type="hidden">
    <input value="aa-your_input_welcome.apg" name="MIval" type="hidden">
    <input value='<tiles:getAsString name="subjectID" />' name="page_id" type="hidden">
    <input value="Your Input Welcome" type="submit">
</form>
