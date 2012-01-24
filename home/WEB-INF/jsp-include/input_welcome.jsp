<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--
<form method="post" action="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>" target="comments" style="display:inline;">
    <!---- define Input Welcome button---->
    <input name="subject" value='<tiles:getAsString name="subjectName" />' type="hidden">
    <input value="aa-your_input_welcome.apg" name="MIval" type="hidden">
    <input value='<tiles:getAsString name="subjectID" />' name="page_id" type="hidden">
    <input value="Your Input Welcome" type="submit">
</form>
--%>
<%--
<button onclick="window.location='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-your_input_welcome.apg&subject=<tiles:getAsString name="subjectName" />&page_id=<tiles:getAsString name="subjectID" />'">Your Input Welcome</button>
--%>


<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-your_input_welcome.apg&subject=<tiles:getAsString name="subjectName"/>&page_id=<tiles:getAsString name="subjectID"/>">
<button>Your Input Welcome</button></a>
