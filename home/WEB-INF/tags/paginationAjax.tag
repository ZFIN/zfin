<!--
This tag can be used to display pagination links for a search result page.
The form bean has to extend PaginationBean to make this work.
-->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="paginationBean" type="org.zfin.framework.presentation.PaginationBean" %>
<%@attribute name="submitMethod" type="java.lang.String" %>

<script type="text/javascript">
    /*<![CDATA[*/
    jQuery("document").ready(function () {

        function loadURL(url) {
            //window.alert('URL: ' + url);
            submitForm(url);
        }


        // Event handlers
        jQuery.address.init(function (event) {
            //jQuery('.history').address();
        }).change(function (event) {
                    window.alert('path: ' + event.path);
                    var hash = event.path;
                    hash = hash ? hash.toLowerCase() : '1';
                    //window.alert('hash: '+hash);
                    //window.alert('event: '+jQuery('[rel=address:' + event.value + ']'));
                    //jQuery("#area").load(jQuery('[rel=address:' + event.value + ']').attr('href'));
//                    window.alert('hash: ' + hash);
                    //if (hash)
                        //loadURL(jQuery('[rel=address:' + event.value + ']').attr('href'));
                });
        jQuery.address.externalChange(function (event) {
                    window.alert('path External: ' + event.path);
                    var hash = event.path;
                    hash = hash ? hash.toLowerCase() : '1';
                    //window.alert('hash: '+hash);
                    //window.alert('event: '+jQuery('[rel=address:' + event.value + ']'));
                    //jQuery("#area").load(jQuery('[rel=address:' + event.value + ']').attr('href'));
//                    window.alert('hash: ' + hash);
                    //if (hash)
                        //loadURL(jQuery('[rel=address:' + event.value + ']').attr('href'));
                });

        jQuery('a').click(function () {
            window.alert('click: '+jQuery(this).attr('href'));
            loadURL(jQuery(this).attr('href'));
        });

    });

    /*]]>*/
</script>

<center>
    <c:if test="${paginationBean.paginationNeeded}">
        <div class="pagination">
            <table>
                <tr>
                    <td align="center" valign="top">
                        <c:choose>
                            <c:when test="${!paginationBean.firstPage}">
                                <a href="javascript:submitFishSearchByPage(${paginationBean.previousPage})">&laquo;
                                    previous</a>
                            </c:when>
                            <c:otherwise>
                                <span class="disabled">&laquo; previous</span>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${paginationBean.showFirstPage}">
                            <a href="javascript:submitFishSearchByPage('1')">1</a>
                        </c:if><c:if test="${paginationBean.elisionForLowerPages}">
                        ...
                    </c:if><c:forEach var="currentPage" items="${paginationBean.pageList}">
                        <c:if test="${currentPage != paginationBean.page}">
                            <a href="${currentPage}" rel="address:${currentPage}">
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
                            <a href="javascript:submitFishSearchByPage(${paginationBean.totalNumPages})">${paginationBean.totalNumPages}</a>
                        </c:if>
                        <c:choose>
                            <c:when test="${!paginationBean.lastPage}">
                                <a href="javascript:submitFishSearchByPage(${paginationBean.nextPage})">next &raquo;</a>
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