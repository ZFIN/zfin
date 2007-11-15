<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="50%">

    <tr><td colspan="3" class="sectionTitle">Servlet Context Information</td></tr>
    <tr>
        <td width="100" class="sectionTitle">Key</td>
        <td class="sectionTitle">Value</td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Server Info: </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.context.serverInfo}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Servlet Context Name: </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.context.servletContextName}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Version: </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.context.majorVersion}"/>.<c:out value="${formBean.context.minorVersion}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Web Root: </td>
        <td colspan="2" class="listContent">
            <c:out value="${formBean.realPath}"/>
        </td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle" colspan="2">Context Attributes</td>
    </tr>
    <c:forEach var="attribute" items="${formBean.attributes}">
        <tr>
            <td class="listContentBold">
                <c:out value='${attribute.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${attribute.value}'/>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td valign=top class="listContentBold">
           JSP Classpath: </td>
        <td colspan="2" class="listContent">
            <c:forEach var="element" items="${formBean.classpath}" >
                <c:out value="${element}" escapeXml="false"/><br>
            </c:forEach>
        </td>
    </tr>
    <tr>
        <td width="100" class="sectionTitle" colspan="2">Initialization Parameters</td>
    </tr>
    <c:forEach var="attribute" items="${formBean.initializationParameters}">
        <tr>
            <td class="listContentBold">
                <c:out value='${attribute.key}'/>
            </td>
            <td class="listContent">
                <c:out value='${attribute.value}'/>
            </td>
        </tr>
    </c:forEach>
</table>
