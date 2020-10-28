<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Zfin Properties Enum Values">
    <table class="table table-hover">
        <c:forEach items="${ZfinPropertiesEnum.values()}" var="enumValue">
            <tr>
                <td>${enumValue}</td>
                <td>${enumValue.value()}</td>
            </tr>
        </c:forEach>
    </table>
</z:devtoolsPage>