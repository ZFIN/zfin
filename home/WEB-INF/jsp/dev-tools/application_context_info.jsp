<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="50%">

    <tr><td colspan="3" class="sectionTitle">We Application Context Information</td></tr>
    <tr>
        <td width="130" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Display Name: </td>
        <td colspan="2" class="listContent">
            ${formBean.applicationContext.displayName}
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Server Start Day: </td>
        <td class="listContent">
            <fmt:formatDate value="${formBean.startup}" type="date"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Server Start Time: </td>
        <td class="listContent">
            <fmt:formatDate value="${formBean.startup}" type="Time"/>
        </td>
    </tr>
</table>
