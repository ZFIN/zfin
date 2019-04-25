<!--
This include can be used to display pagination links for a search result page
This include expects the Pagination bean in request scope with the name <formBean>
-->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<center><c:if test="${formBean.paginationNeeded}">
    <div class="pagination">
        <table>
            <tr>
                <td align="center" valign="top">
                    <c:choose>
                        <c:when test="${!formBean.firstPage}">
                            <a href="webdriver?${formBean.actionUrl}&START=${formBean.firstRecordOnPreviousPage}&page=${formBean.pageInteger -1}">&laquo; previous</a>
                        </c:when>
                        <c:otherwise>
                            <span class="disabled">&laquo; previous</span>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${formBean.showFirstPage}">
                        <a href="webdriver?${formBean.actionUrl}&START=${formBean.firstRecordOnFirstPage}&page=1">1</a>
                    </c:if>
                    <c:if test="${formBean.elisionForLowerPages}">
                        ...
                    </c:if>
                    <c:forEach var="currentPage" items="${formBean.firstRecordOnPageList}">
                        <c:if test="${currentPage.key != formBean.page}">
                            <a href="webdriver?${formBean.actionUrl}&START=${currentPage.value}&page=${currentPage.key}">
                                    ${currentPage.key}
                            </a>
                        </c:if>
                        <c:if test="${currentPage.key == formBean.page}">
                            <span class="current">${currentPage.key}</span>
                        </c:if>
                    </c:forEach>
                    <c:if test="${formBean.elisionForHigherPages}">
                        ...
                    </c:if>
                    <c:if test="${formBean.showLastPage}">
                        <a 
href="webdriver?${formBean.actionUrl}&START=${formBean.firstRecordOnLastPage}&page=${formBean.totalNumPages}">${formBean.totalNumPages}</a>
                    </c:if>
                    <c:choose>
                        <c:when test="${!formBean.lastPage}">
                            <a href="webdriver?${formBean.actionUrl}&START=${formBean.firstRecordOnNextPage}&page=${formBean.pageInteger +1}">next &raquo;</a>
                        </c:when>
                        <c:otherwise>
                    <span class="disabled">
                    next &raquo;
                        </span>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </table>
    </div>
</c:if>
</center>
