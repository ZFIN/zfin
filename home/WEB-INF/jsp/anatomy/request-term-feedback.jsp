<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="50%">
    <tr>
        <td>
            Dear ${formBean.firstname}:
        </td>
    </tr>
    <tr>
        <td>
            Thank you for using ZFIN. Your suggestion is very important to us.
            We will respond to it as soon as possible.
        </td>
    </tr>
    <tr>
        <td>
            Your new anatomical structure description is: <br>
            <textarea name="termDetail" cols=60 rows=8 disabled="disabled"> ${formBean.termDetail} </textarea> <br>
        </td>
    </tr>
    <tr>
        <td>
            Regards,<br>
            Zebrafish Information Network
        </td>
    </tr>
</table>
<input value="Close" onclick="window.close()" type="button">





