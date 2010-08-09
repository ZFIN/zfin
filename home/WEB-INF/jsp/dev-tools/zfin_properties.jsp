<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<form action="/action/dev-tools/zfin-properties" method="post">
    <input type="submit" value="Save"/>
    <table border="1">
        <%
            for(ZfinPropertiesEnum zfinPropertiesEnum : ZfinPropertiesEnum.values()){
        %>
        <tr><td>
            <%=zfinPropertiesEnum.name() %>
        </td>
            <td><input name="<%=zfinPropertiesEnum.name()%>" value="<%=zfinPropertiesEnum.value()%>"/>
            </td>
        </tr>
        <% } %>
    </table>
    <input type="submit" value="Save"/>
</form>
