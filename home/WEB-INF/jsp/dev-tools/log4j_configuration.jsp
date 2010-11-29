<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="loggerForm" class="org.zfin.framework.presentation.Log4JConfigurationFormBean" scope="request"/>

<table width="500">
    <tr>
        <td class="item">Loggers that default to an inherited value are not shown.
        </td>
        <td align="right" class="item">
        </td>
    </tr>
</table>

<form:form method="GET" action="/action/dev-tools/log4j-configuration" commandName="loggerForm">
    <table width="500">
        <tr bgcolor="#ccccc0">
            <td class="bold left-align">Name</td>
            <td class="bold left-align">Level</td>
            <td class="bold left-align">Update</td>
        </tr>

        <c:forEach var="logger" items="${loggerForm.allLoggers}">
            <c:if test="${logger.level != null}">
                <tr class="even">
                    <td>
                            ${logger.name}
                    </td>
                    <input type="hidden" name="loggerName" value="${logger.name}"/>
                    <td>
                            ${logger.level}
                    </td>
                    <td>
                        <form:select path="level" multiple="single">
                            <c:forEach var="loggerVar" items="${loggerForm.loggerValues}">
                                <option ${(loggerVar.value eq logger.level ? "selected":"")}>${loggerVar.value}</option>
                            </c:forEach>
                        </form:select>
                    </td>
                </tr>
            </c:if>
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
                    <form:options items="${loggerForm.loggerValues}" />
                </form:select>
            <td><input type="submit" name="type" value="create"/></td>
        </tr>
    </table>
        <p/>
    <div style="width:100%;height:10px;background-color:gray;" ></div>
    <p/>
    
    <table>
        <tr>
            <td class="bold left-align">Appender</td>
        </tr>
        <tr bgcolor="#ccccc0">
            <td class="bold left-align">Name</td>
            <td class="bold left-align">Layout Content Type</td>
        </tr>

        <c:forEach var="appender" items="${loggerForm.appenders}">
            <tr class="even ">
                <td>
                    ${appender.name}
                </td>
                <td>
                    ${appender.layout.contentType}
                </td>
            </tr>
        </c:forEach>
    </table>
</form:form>
