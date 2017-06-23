<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="pubListBean" type="org.zfin.publication.presentation.PublicationListBean" required="true" %>
<%@ attribute name="url" type="java.lang.String" rtexprvalue="true" required="true" %>

<c:if test="${pubListBean.numOfPublishedPublications > 1 || pubListBean.numOfUnpublishedPublications > 1}">
    <input type=button name=resultOrder
           onClick="orderByDate()"
           value="Order By Date" id="orderByDate">
    <input type=button name=resultOrder
           onClick="orderByAuthor()"
           value="Order By Author" style="display: none" id="orderByAuthor">
</c:if>
<c:if test="${pubListBean.numOfPublishedPublications > 0}">
    <table class="summary rowstripes sortable" id="pubsByAuthor">
        <c:forEach var="pub" items="${pubListBean.sortedPublishedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <div class="show_pubs">
                        <a href="/${pub.zdbID}"
                           id="${pub.zdbID}">${pub.authors}&nbsp;(${pub.year})&nbsp;${pub.title}.&nbsp;${pub.journal.abbreviation}&nbsp;<c:if
                                test="${pub.volume != null}">${pub.volume}:</c:if>${pub.pages}</a>
                        <authz:authorize access="hasRole('root')"><c:if
                                test="${pub.open}">OPEN</c:if><c:if
                                test="${!pub.open}">CLOSED</c:if><c:if
                                test="${pub.indexed}">, INDEXED</c:if>
                        </authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
    <table class="summary rowstripes sortable" id="pubsByDate" style="display: none">
        <c:forEach var="pub" items="${pubListBean.publishedPublicationsByDate}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <div class="show_pubs">
                        <a href="/${pub.zdbID}"
                           id="${pub.zdbID}">${pub.authors}&nbsp;(${pub.year})&nbsp;${pub.title}.&nbsp;${pub.journal.abbreviation}&nbsp;<c:if
                                test="${pub.volume != null}">${pub.volume}:</c:if>${pub.pages}</a>
                        <authz:authorize access="hasRole('root')"><c:if
                                test="${pub.open}">OPEN</c:if><c:if
                                test="${!pub.open}">CLOSED</c:if><c:if
                                test="${pub.indexed}">, INDEXED</c:if>
                        </authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>

<c:if test="${pubListBean.numOfUnpublishedPublications > 0}">
    <hr>
    <b>Additional Citations (${pubListBean.numOfUnpublishedPublications}):</b>
    <table class="summary rowstripes" id="unpublishedByAuthor">
    <c:forEach var="pub" items="${pubListBean.sortedUnpublishedPublications}"
               varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td>
                <div class="show_pubs">
                    <a href="/${pub.zdbID}">${pub.authors}&nbsp;(${pub.year})&nbsp;${pub.title}</a>
                    <authz:authorize access="hasRole('root')"><c:if
                            test="${pub.open}">OPEN</c:if><c:if
                            test="${!pub.open}">CLOSED</c:if><c:if
                            test="${pub.indexed}">, INDEXED</c:if>
                    </authz:authorize>
                </div>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
    </table>
    <table class="summary rowstripes" id="unpublishedByDate" style="display: none">
        <c:forEach var="pub" items="${pubListBean.unpublishedByDate}"
                   varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <div class="show_pubs">
                        <a href="/${pub.zdbID}">${pub.authors}&nbsp;(${pub.year})&nbsp;${pub.title}</a>
                        <authz:authorize access="hasRole('root')"><c:if
                                test="${pub.open}">OPEN</c:if><c:if
                                test="${!pub.open}">CLOSED</c:if><c:if
                                test="${pub.indexed}">, INDEXED</c:if>
                        </authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>

<script>
    function orderByDate() {
        jQuery("#pubsByAuthor").hide();
        jQuery("#pubsByDate").show();
        jQuery("#unpublishedByAuthor").hide();
        jQuery("#unpublishedByDate").show();
        jQuery("#orderByDate").hide();
        jQuery("#orderByAuthor").show();
    }

    function orderByAuthor() {
        jQuery("#pubsByAuthor").show();
        jQuery("#pubsByDate").hide();
        jQuery("#unpublishedByAuthor").show();
        jQuery("#unpublishedByDate").hide();
        jQuery("#orderByDate").show();
        jQuery("#orderByAuthor").hide();
    }
</script>
