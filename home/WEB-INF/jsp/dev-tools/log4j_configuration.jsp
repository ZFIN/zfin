<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<table width="500">
    <tr>
        <td class="item">Loggers that default to an inherited value are not shown.
        </td>
        <td align="right" class="item">
            <%--
                                <logic:equal value="true" property="showAllLevels" name="loggerForm">
                                    <a href="/spring/dev-tools/log4j-configuration?showAllLevels=false">Hide Default Loggers</a>
                                </logic:equal>
                                <logic:equal value="false" property="showAllLevels" name="loggerForm">
                                    <a href="/spring/dev-tools/log4j-configuration?showAllLevels=true">Show All Loggers</a>
                                </logic:equal>
            --%>
        </td>
    </tr>
</table>

<form:form method="GET" action="/action/dev-tools/log4j-configuration" commandName="loggerForm">
    <table width="500">
        <tr>
            <td class="item-bold">Name</td>
            <td class="item-bold">Level</td>
            <td></td>
        </tr>

        <c:forEach var="logger" items="${loggerForm.allLoggers}">
            <logic:notEmpty name="logger" property="level">
                <tr>
                    <td class="item">
                        <bean:write name="logger" property="name"/>
                    </td>
                    <input type="hidden" name="loggerName" value="<c:out value='${logger.name}' />" />
                    <td class="item">
                        <c:out value="${logger.level}" />
                    </td>
                    <td class="item">
                        <form:select path="level" multiple="single">
                            <form:options items="${loggerForm.loggerValues}" itemLabel="value" itemValue="key" />
                        </form:select>
                    </td>
                </tr>
            </logic:notEmpty>
        </c:forEach>
    </table>

    <input type="submit" name="type" value="update"/>
    <br/>
    <br/>
    <br/>
    <table>
        <tr>
            <td class="item">Create a new logger with name:</td>
            <td class="item">
                <input type="text" name="newLoggerName"/>
            </td>
            <td class="item">
                <form:select path="newLoggerLevel" multiple="single">
                    <form:options items="${loggerForm.loggerValues}" itemLabel="value" itemValue="key" />
                </form:select>
            <td><input type="submit" name="type" value="create"/></td>
        </tr>
    </table>
</form:form>
