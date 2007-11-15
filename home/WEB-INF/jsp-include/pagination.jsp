<!--
This include can be used to display pagination links for a search result page

This include expects a bean in request scope with the name <formBean>
-->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script type="text/javascript">
    function submitForm(pageNum) {
        document.pagination.page.value = pageNum;
        document.pagination.submit();
        return true;
    }
</script>

<center><c:if test="${formBean.paginationNeeded}">
    <table width="100%" bgcolor="#eeeeee">
        <tr>
            <center>
                <font size="3">
                    <c:if test="${formBean.totalRecords != 0}">
                        <b>Displaying records <c:out value="${formBean.firstRecord}"/>-<c:if
                                test="${formBean.lastRecord < formBean.totalRecords}"><c:out
                                value="${formBean.lastRecord}"/>
                        </c:if><c:if test="${formBean.lastRecord >= formBean.totalRecords}"><c:out
                                value="${formBean.totalRecords}"/></c:if> of <c:out value="${formBean.totalRecords}"/>
                            total
                            <c:if test="${formBean.totalNumPages > 1}">(Page <c:out value="${formBean.page}"/> of <c:out
                                    value="${formBean.totalNumPages}"/>)</c:if></b>
                    </c:if>
                    <c:if test="${formBean.totalRecords == 0}">
                        <b>No records were found matching your query.</b><br><br>
                    </c:if>
                </font>
            </center>
        </tr>
    </table>

    <form action="" method="POST" name="pagination">
        <input type="hidden" name="page"/>
        <table width="=300">
            <tr>
                <td width="40" align="right">
                    <logic:notEqual value="true" name="formBean" property="isFirstPage">
                        <a href="javascript:submitForm(<c:out value="${formBean.previousPage}"/>)">Previous</a>&nbsp;
                    </logic:notEqual>
                    <logic:equal value="true" name="formBean" property="isFirstPage">
                        &nbsp;
                    </logic:equal>
                </td>
                <td align="center" nowrap="true">
                    <logic:iterate id="currentPage" name="formBean" property="pageList" type="java.lang.Integer">
                        <logic:notEqual value="<%=currentPage.toString() %>" name="formBean" property="page">
                            &nbsp; <a href="javascript:submitForm(<c:out value="${currentPage}"/>);"><c:out
                                value="${currentPage}"/></a>
                        </logic:notEqual>
                        <logic:equal value="<%=currentPage.toString() %>" name="formBean" property="page">
                            <c:out value="${currentPage}"/>&nbsp;
                        </logic:equal>
                    </logic:iterate>
                </td>
                <td width="40" align="left">
                    <logic:notEqual value="true" name="formBean" property="isLastPage">
                        <a href="javascript:submitForm(<c:out value="${formBean.nextPage}"/>);">Next</a>
                    </logic:notEqual>
                    <logic:equal value="true" name="formBean" property="isLastPage">
                        &nbsp;
                    </logic:equal>
                </td>
            </tr>
            <tr>
                <td>
                    <logic:notEqual value="true" name="formBean" property="isFirstPage">
                        <a href="javascript:submitForm(1)"> First </a>
                    </logic:notEqual>
                    <logic:equal value="true" name="formBean" property="isFirstPage">
                        &nbsp;
                    </logic:equal>
                </td>
                <td>
                    &nbsp;
                </td>
                <td>
                    <logic:notEqual value="true" name="formBean" property="isLastPage">
                        <a href="javascript:submitForm(<c:out value="${formBean.totalNumPages}"/>);">
                            Last
                        </a>
                    </logic:notEqual>
                    <logic:equal value="true" name="formBean" property="isLastPage">
                        &nbsp;
                    </logic:equal>
                </td>
            </tr>
        </table>
    </form>
</c:if>
</center>