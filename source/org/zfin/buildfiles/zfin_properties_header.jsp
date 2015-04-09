<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<p>
<div id="errorMessageDiv" style="color: red;"></div>
<form action="/action/dev-tools/zfin-properties" method="post" >


    <%--check error here--%>
        <%
        String errorString = "";
//        errorString += "</ul>\n";
        %>

    <input type="submit" value="Update"/>
    <table border="1">
        <tr>
            <th>Key</th>
            <th>VM Value</th>
        </tr>
            <%
        ZfinPropertiesEnum zfinPropertiesEnum[] = ZfinPropertiesEnum.values() ;
        int i = 0 ;
    %>

