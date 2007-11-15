<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


    <table width="500">
        <tr>
            <logic:equal value="false" property="showAllLevels" name="log4JForm">
            <td class="item">Loggers that default to an inherited value are not shown.
                             </td>
            </logic:equal>
<%--
            <td align="right"  class="item">
            <logic:equal value="true" property="showAllLevels" name="log4JForm">
                <a href="/action/edit-log4j-configuration?showAllLevels=false">Hide Default Loggers</a>
            </logic:equal>
            <logic:equal value="false" property="showAllLevels" name="log4JForm">
                <a href="/action/edit-log4j-configuration?showAllLevels=true">Show All Loggers</a>
            </logic:equal>
            </td>
--%>
        </tr>
    </table>

    <html:form method="POST" action="dev-tools/edit-log4j-configuration">   
        <table width="500">
        <tr>
            <td class="item-bold">Name</td>
            <td class="item-bold">Level</td>
            <td></td>
        </tr>

        <logic:iterate id="logger" name="log4JForm" property="allLoggers" type="org.apache.log4j.Logger" >
        <logic:notEmpty name="logger" property="level" >
        <tr>
            <td class="item">
                <bean:write name="logger" property="name" />
            </td>
                <input type="hidden" name="loggerName" value="<%=logger.getName()%>"/>
            <td class="item">
                <bean:write name="logger" property="level" />
            </td>
            <td class="item">
                <html:select name="logger" property="level" >
                    <html:optionsCollection name="log4JForm" property="allLoggerValues"/>
                </html:select>
            </td>
        </tr>
        </logic:notEmpty>
     </logic:iterate>
        </table>

        <input type="submit" name="type" value="update"/>
        <br/>
        <br/>
        <br/>
        <table>
        <tr>
            <td class="item">Create a new logger with name:</td>
            <td class="item"><html:text property="newLoggerName" /></td>
            <td class="item">
                        <html:select name="log4JForm" property="newLoggerLevel" >
                            <html:optionsCollection name="log4JForm" property="allLoggerValues"/>
                        </html:select>

            <td><input type="submit" name="type" value="create"/></td>
        </tr>
        </table>
    </html:form>
