<%@ page import="org.zfin.framework.presentation.PaginationBean" %>
<!--
This include can be used to display pagination links for a search result page
This include expects the Pagination bean in request scope with the name <formBean>
-->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/pagination.css"/>

<center><c:if test="${formBean.paginationNeeded}">
    <div class="pagination">
        <table width="=430">
            <tr>
                <td width="100" align="right" valign="top">
                    <c:choose>
                        <c:when test="${!formBean.firstPage}">
                            <a href="${formBean.actionUrl}&<%= PaginationBean.PAGE%>=${formBean.previousPage}">&laquo; Previous</a>
                        </c:when>
                        <c:otherwise>
                            <span class="disabled">&laquo; Previous</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td align="center" valign="top" nowrap="true">
                    <c:if test="${formBean.showFirstPage}">
                        <a href="${formBean.actionUrl}&<%= PaginationBean.PAGE%>=1">1</a>
                    </c:if>
                    <c:if test="${formBean.elisionForLowerPages}">
                        ...
                    </c:if>
                    <c:forEach var="currentPage" items="${formBean.pageList}">
                        <c:if test="${currentPage != formBean.page}">
                            &nbsp;
                            <a href="${formBean.actionUrl}&<%= PaginationBean.PAGE%>=${currentPage}">
                                    ${currentPage}</a>
                        </c:if>
                        <c:if test="${currentPage == formBean.page}">
                            &nbsp; ${currentPage}
                        </c:if>
                    </c:forEach>
                    <c:if test="${formBean.elisionForHigherPages}">
                        ...
                    </c:if>
                    <c:if test="${formBean.showLastPage}">
                        <a href="${formBean.actionUrl}&<%= PaginationBean.PAGE%>=${formBean.totalNumPages}">${formBean.totalNumPages}</a>
                    </c:if>
                </td>
                <td width="100" valign="top" align="left">
                    <c:choose>
                        <c:when test="${!formBean.lastPage}">
                            <a href="${formBean.actionUrl}&<%= PaginationBean.PAGE%>=${formBean.nextPage}">Next &raquo;</a>
                        </c:when>
                        <c:otherwise>
                    <span class="disabled">
                    Next &raquo;
                        </span>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </table>
    </div>
</c:if>
</center>