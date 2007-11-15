<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="sectionTitle">Error Page</div>
<p/>
<span style="color:crimson;">An error occured.</span>

<table>
    <tr>
        <td>
            Error Message:
        </td>
        <td>
            <c:if test="${exception != null}">
                ${exception.message}
            </c:if>
        </td>
    </tr>
    <authz:authorize ifAnyGranted="root">
        <tr>
            <td valign="top">
                Stack Trace:
            </td>
            <td style="font-size:12">
                <c:forEach var="element" items="${exception.stackTrace}">
                    ${element} <br/>
                </c:forEach>
            </td>
        </tr>
        <tr>
            <td valign="top">
                Query parameters:
            </td>
            <td>
                <table style="font-size:12">
                    <c:forEach var="element" items="${param}">
                        <tr>
                            <td>${element.key}=${element.value}</td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
        </tr>
    </authz:authorize>
</table>