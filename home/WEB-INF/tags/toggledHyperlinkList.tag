<%--
  This tag creates a list of hyperlink for a list of anatomy terms.
  If the list is bigger than a given maximum number we only display
  the max number of terms, add the total number and offer a link to expand to
  see the total list.

  Parameters:
    collection: the object that holds all ao terms
    maxNumber:  the max number of terms to display for the short version
    id:   A unique span ID, should be the zdbID of the entity being listed.

    Check out:  http://www.oracle.com/technology/pub/articles/cioroianu_tagfiles.html

 --%>

<%@ tag body-content="scriptless" %>
<%@attribute name="collection" type="java.util.Collection" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@attribute name="id" type="java.lang.String" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div>
    <c:if test="${fn:length(collection) > 0 }">
        <c:choose>
            <c:when test="${fn:length(collection) > maxNumber }">
            <span style="display:inline;" id="${id}-short">
            <c:forEach var="hyperlinkEntity" items="${collection}" varStatus="aoIndex" end="${maxNumber -1}">
                <zfin:link entity="${hyperlinkEntity}"/><c:if test="${!aoIndex.last}">, </c:if>
            </c:forEach>
                <nobr>
                    (<a href="javascript:onClick=showAnatomyList('${id}', true)">all ${fn:length(collection)}</a>)
                    <img onclick="showAnatomyList('${id}', true)"
                         src="/images/right_arrow.gif" alt="expand" title="Show all ${fn:length(collection)} terms">
                </nobr>
                </span>
            <span style="display:none;" id="${id}-long">
            <c:forEach var="hyperlinkEntity" items="${collection}" varStatus="aoIndex">
                <zfin:link entity="${hyperlinkEntity}"/><c:if test="${!aoIndex.last}">, </c:if>
            </c:forEach>&nbsp;
                <img onclick="showAnatomyList('${id}', false)"
                     src="/images/left_arrow.gif" alt="collapse" title="Show only first ${maxNumber+1} terms">
                </span>
            </c:when>
            <c:otherwise>
                <c:forEach var="hyperlinkEntity" items="${collection}" varStatus="aoIndex">
                    <zfin:link entity="${hyperlinkEntity}"/><c:if test="${!aoIndex.last}">, </c:if>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>
<script type="text/javascript">
    function showAnatomyList(element_id, displayLongVersion) {
        var shortElement = document.getElementById(element_id + '-short');
        var longElement = document.getElementById(element_id + '-long');
        if (displayLongVersion) {
            shortElement.style.display = "none";
            longElement.style.display = "inline";
        } else {
            shortElement.style.display = "inline";
            longElement.style.display = "none";
        }
    }
</script>
