<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${formBean.validPublication}">
        <table>
            <tr>
                <td valign="top" style="font-weight:bold;">

                </td>
                <td>
                    <zfin:link entity="${formBean.publication}"/>
                </td>
            </tr>
            <tr>
                <td valign="top" style="font-weight:bold;">
                    Title:
                </td>
                <td>
                        ${formBean.publication.title}
                </td>
            </tr>
            <tr>
                <td valign="top" style="font-weight:bold;">
                    Authors:
                </td>
                <td>
                    <zfin2:toggleTextLength text="${formBean.publication.authors}" idName="1" shortLength="80"/>
                </td>
            </tr>
            <tr>
                <td valign="top" style="font-weight:bold;">
                    ZDB ID:
                </td>
                <td>
                        ${formBean.publication.zdbID}
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <hr>
                </td>
            </tr>
        </table>
    </c:when>
    <c:otherwise>
        <center><font size=4>
        <c:if test="${formBean.zdbID != null}">
            Requested ID: <span style="color:red"> ${formBean.zdbID}</span> not found on ZFIN.
        </c:if>
        <c:if test="${formBean.zdbID == null}">
            No ZDB ID
        </c:if>
        </font>
        <hr/>
    </c:otherwise>
</c:choose>
