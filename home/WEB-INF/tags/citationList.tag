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
        <zfin2:publications publications="${pubListBean.sortedPublishedPublications}" />
    </table>
    <table class="summary rowstripes sortable" id="pubsByDate" style="display: none">
        <zfin2:publications publications="${pubListBean.publishedPublicationsByDate}" />
    </table>
</c:if>

<c:if test="${pubListBean.numOfUnpublishedPublications > 0}">
    <hr>
    <b>Additional Citations (${pubListBean.numOfUnpublishedPublications}):</b>
    <table class="summary rowstripes" id="unpublishedByAuthor">
        <zfin2:publications publications="${pubListBean.sortedUnpublishedPublications}" />
    </table>
    <table class="summary rowstripes" id="unpublishedByDate" style="display: none">
        <zfin2:publications publications="${pubListBean.unpublishedByDate}" />
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
