<!--
This include shows the info of the search results:
How many records, pages and total numbers as well

This include expects a bean in request scope with the name <formBean>
-->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%">
    <center>
        <tr>
            <td colspan="3">
                <center>
                    <font size="3">
                        <c:if test="${formBean.totalRecords != 0}">
                            <b>Displaying records <c:out value="${formBean.firstRecord}"/>-<c:if
                                    test="${formBean.lastRecord < formBean.totalRecords}"><c:out
                                    value="${formBean.lastRecord}"/>
                            </c:if><c:if test="${formBean.lastRecord >= formBean.totalRecords}"><c:out
                                    value="${formBean.totalRecords}"/></c:if> of <c:out
                                    value="${formBean.totalRecords}"/> total
                                <c:if test="${formBean.totalNumPages > 1}">(Page <c:out value="${formBean.page}"/> of
                                    <c:out
                                            value="${formBean.totalNumPages}"/>)</c:if></b>
                        </c:if>
                        <c:if test="${formBean.totalRecords == 0}">
                            <b>No records were found matching your query.</b><br><br>
                        </c:if>
                    </font>
                </center>
            </td>
        </tr>
    </center>
</table>
