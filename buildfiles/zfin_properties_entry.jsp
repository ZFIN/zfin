
<tr id="row<%=i%>">
    <td>
        @KEY@

        <%--// (do check on key)!--%>
        <%
            if(false=="@KEY@".equals(zfinPropertiesEnum[i].name())){
                errorString += "@KEY@ " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@@KEY@@".equals(zfinPropertiesEnum[i].value()) || false=="@VALUE@".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("@KEY@")){
                    errorString += "@KEY@ " ;
                    %>
        <font color="red">Deployed value: '@@KEY@@' or '@VALUE@' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>

