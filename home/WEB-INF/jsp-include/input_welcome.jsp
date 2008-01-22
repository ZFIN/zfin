<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table align="right" width="100%">
    <tbody>
        <tr>
            <form method="post" action="/@WEBDRIVER_LOC@/webdriver" target="comments">
                <!---- define Input Welcome button---->
                <input name="subject" value='<tiles:getAsString name="subjectName" />' type="hidden">
                <input value="aa-your_input_welcome.apg" name="MIval" type="hidden">
                <input value='<tiles:getAsString name="subjectID" />' name="page_id" type="hidden">
                <td align="right">
                    <input value="Your Input Welcome" type="submit"></td>
            </form>
        </tr>
    </tbody>
</table>
