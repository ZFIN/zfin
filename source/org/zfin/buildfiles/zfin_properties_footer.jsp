

</table>

<input type="submit" value="Update"/>
</form>

<%
    if(errorString!=null && errorString.length()>0){
        errorString = "Mismatches: " + errorString.replaceAll("  ",",") ;
        %>
     <script type="text/javascript">
         document.getElementById("errorMessageDiv").innerHTML ="<%=errorString%>" ;
     </script>
<%
    }
%>

