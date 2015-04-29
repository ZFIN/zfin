<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%--
  This tag creates a list of hyperlink for a list of entities.
  At a minimum, they must be supported by CreateLinkTag.java,
  to do attribution as well, they need to be supported by AttributionTag.java.

  If the list is bigger than a given maximum number we only display
  the max number of terms, add the total number and offer a link to expand to
  see the total list.

  Parameters:
    collection: the object that holds all ao terms
    maxNumber:  the max number of terms to display for the short version
    id:   A unique span ID, should be the zdbID of the entity being listed.



 --%>

<%@ tag body-content="scriptless" %>
<%@attribute name="collection" type="java.util.Collection" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@attribute name="id" type="java.lang.String" %>
<%@attribute name="showAttributionLinks" type="java.lang.Boolean" required="false" %>
<%@attribute name="showOrderLinks" type="java.lang.Boolean" required="false" %>
<%@attribute name="commaDelimited" type="java.lang.Boolean" required="false" %>


<c:if test="${showAttributionLinks == null}">
    <c:set var="showAttributionLinks" value="false"/>
</c:if>

    <c:if test="${fn:length(collection) > 0 }">
        <c:choose>
            <c:when test="${fn:length(collection) > maxNumber }">
            <span style="display:inline;" id="${id}-short">
            <c:forEach var="entity" items="${collection}" varStatus="loop" end="${maxNumber -1}">
                <zfin:link entity="${entity}"/><c:if test="${showAttributionLinks}"> <zfin:attribution entity="${entity}"/></c:if>


                <c:if test="${(!loop.last) && (commaDelimited)}">, </c:if>
                <c:if test="${(!loop.last) && (!commaDelimited)}"><br/></c:if>
            </c:forEach>
                <nobr>
                    (<a href="javascript:onClick=showEntityList('${id}', true)">all ${fn:length(collection)}</a>)
                    <img onclick="showEntityList('${id}', true)" class="clickable"
                         src="/images/right_arrow.gif" alt="expand" title="Show all ${fn:length(collection)} terms">
                </nobr>
                </span>
            <span style="display:none;" id="${id}-long">
            <c:forEach var="entity" items="${collection}" varStatus="loop">
                <zfin:link entity="${entity}"/><c:if test="${showAttributionLinks}"> <zfin:attribution entity="${entity}"/></c:if>

                <c:if test="${(!loop.last) && (commaDelimited)}">, </c:if>
                <c:if test="${(!loop.last) && (!commaDelimited)}"><br/></c:if>
            </c:forEach>&nbsp;
                <img onclick="showEntityList('${id}', false)"  class="clickable"
                     src="/images/left_arrow.gif" alt="collapse" title="Show only first ${maxNumber+1} terms">
                </span>
            </c:when>
            <c:otherwise>
                <c:forEach var="entity" items="${collection}" varStatus="loop">
                    <zfin:link entity="${entity}"/><c:if test="${showAttributionLinks}"> <zfin:attribution entity="${entity}"/></c:if>

                    <c:if test="${(!loop.last) && (commaDelimited)}">, </c:if>
                    <c:if test="${(!loop.last) && (!commaDelimited)}"><br/></c:if>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>

<script type="text/javascript">
    function showEntityList(element_id, displayLongVersion) {
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
