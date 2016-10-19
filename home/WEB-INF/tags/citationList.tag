<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="pubListBean" type="org.zfin.publication.presentation.PublicationListBean" required="true" %>
<%@ attribute name="url" type="java.lang.String" rtexprvalue="true" required="true" %>

<c:if test="${pubListBean.numOfPublishedPublications > 0}">
    <table class="summary rowstripes sortable">
            <c:if test="${pubListBean.numOfPublishedPublications > 1}">
                <input type=button name=resultOrder
                       onClick="orderByDate()"
                       value="Order By Date" id="orderByDate">
                <input type=button name=resultOrder
                       onClick="orderByAuthor()"
                       value="Order By Author" style="display: none" id="orderByAuthor">
            </c:if>
        </caption>
        <tr style="display: none">
            <th>date</th>
            <th>Pub</th>
        </tr>
        <c:forEach var="pub" items="${pubListBean.sortedPublishedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td style="display: none">${pub.year}</td>
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
    <table class="summary rowstripes">
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
</c:if>
</table>

<script>
    function orderByDate() {
        var myTH = document.getElementsByTagName("th")[0];
        myTH.click();
        myTH.click();
        jQuery("#orderByDate").hide();
        jQuery("#orderByAuthor").show();
    }

    function orderByAuthor() {
        var myTH = document.getElementsByTagName("th")[1];
        myTH.click();
        myTH.click();
        jQuery("#orderByDate").show();
        jQuery("#orderByAuthor").hide();
    }
</script>
