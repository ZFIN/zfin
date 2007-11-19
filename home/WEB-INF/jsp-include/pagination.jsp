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
                    <c:if test="${!formBean.firstPage}">
                        <a href="javascript:submitForm(${formBean.previousPage})">Previous</a>&nbsp;
                    </c:if>
                    <c:if test="${!formBean.firstPage}">
                        &nbsp;
                    </c:if>
                </td>
                <td align="center" nowrap="true">
                    <c:forEach var="currentPage" items="${formBean.pageList}">
                        <c:if test="${currentPage != formBean.page}">
                            &nbsp;
                            <a href="javascript:submitForm(${currentPage})">
                                    ${currentPage}
                            </a>
                        </c:if>
                        <c:if test="${currentPage == formBean.page}">
                            &nbsp; ${currentPage}
                        </c:if>
                    </c:forEach>
                </td>
                <td width="40" align="left">
                    <c:if test="${!formBean.lastPage}">
                        <a href="javascript:submitForm(${formBean.nextPage});">Next</a>
                    </c:if>
                    <c:if test="${!formBean.lastPage}">
                        &nbsp;
                    </c:if>
                </td>
            </tr>
            <tr>
                <td>
                    <c:if test="${!formBean.firstPage}">
                        <a href="javascript:submitForm(1)"> First </a>
                    </c:if>
                    <c:if test="${formBean.firstPage}">
                        &nbsp;
                    </c:if>
                </td>
                <td>
                    &nbsp;
                </td>
                <td>
                    <c:if test="${!formBean.lastPage}">
                        <a href="javascript:submitForm(<c:out value="${formBean.totalNumPages}"/>);">
                            Last
                        </a>
                    </c:if>
                    <c:if test="${formBean.lastPage}">
                        &nbsp;
                    </c:if>
                </td>
            </tr>
        </table>
    </form>
</c:if>
</center>