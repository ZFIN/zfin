<%@ tag import="org.zfin.framework.presentation.PaginationBean" %>
<!--
This tag can be used to display pagination links for a search result page.
The form bean has to extend PaginationBean to make this work.
-->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="paginationBean" type="org.zfin.framework.presentation.PaginationBean" %>

<link rel="stylesheet" type="text/css" href="/css/pagination.css"/>
<center>
    <c:if test="${paginationBean.paginationNeeded}">
        <div class="pagination">
            <table>
                <tr>
                    <td align="center" valign="top">
                        <c:choose>
                            <c:when test="${!paginationBean.firstPage}">
                                <a href="${paginationBean.actionUrl}<%= PaginationBean.PAGE%>=${paginationBean.previousPage}">&laquo;
                                    previous</a>
                            </c:when>
                            <c:otherwise>
                                <span class="disabled">&laquo; previous</span>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${paginationBean.showFirstPage}">
                            <a href="${paginationBean.actionUrl}<%= PaginationBean.PAGE%>=1">1</a>
                        </c:if><c:if test="${paginationBean.elisionForLowerPages}">
                            ...
                        </c:if><c:forEach var="currentPage" items="${paginationBean.pageList}">
                            <c:if test="${currentPage != paginationBean.page}">
                                <a href="${paginationBean.actionUrl}<%= PaginationBean.PAGE%>=${currentPage}">
                                        ${currentPage}</a>
                            </c:if>
                            <c:if test="${currentPage == paginationBean.page}">
                               <span class="current"> ${currentPage}</span>
                            </c:if>
                        </c:forEach>
                        <c:if test="${paginationBean.elisionForHigherPages}">
                            ...
                        </c:if>
                        <c:if test="${paginationBean.showLastPage}">
                            <a href="${paginationBean.actionUrl}<%= PaginationBean.PAGE%>=${paginationBean.totalNumPages}">${paginationBean.totalNumPages}</a>
                        </c:if>
                        <c:choose>
                            <c:when test="${!paginationBean.lastPage}">
                                <a href="${paginationBean.actionUrl}<%= PaginationBean.PAGE%>=${paginationBean.nextPage}">next &raquo;</a>
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