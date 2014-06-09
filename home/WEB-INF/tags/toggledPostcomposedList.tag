<%--
  This tag creates a list of hyperlink for a list of entities.
  At a minimum, they must be supported by CreateLinkTag.java,
  to do attribution as well, they need to be supported by AttributionTag.java.

  If the list is bigger than a given maximum number we only display
  the max number of terms, add the total number and offer a link to expand to
  see the total list.

  Parameters:
    expressionResults: the object that holds all expression result objects
    maxNumber:  the max number of terms to display for the short version
    id:   A unique span ID, should be the zdbID of the entity being listed.

 --%>

<%@ tag body-content="scriptless" %>
<%@attribute name="entities" type="java.util.Collection" %>
<%@attribute name="numberOfEntities" type="java.lang.Integer" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@attribute name="id" type="java.lang.String" required="false" %>
<%@attribute name="showAttributionLinks" type="java.lang.Boolean" required="false" %>
<%@attribute name="suppressPopupLinks" type="java.lang.Boolean" required="false" %>
<%@attribute name="useAjaxForLongVersion" type="java.lang.Boolean" required="false" %>
<%@attribute name="ajaxLink" type="java.lang.String" required="false" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${showAttributionLinks == null}">
    <c:set var="showAttributionLinks" value="false"/>
</c:if>

<c:if test="${suppressPopupLinks == null}">
    <c:set var="suppressPopupLinks" value="false"/>
</c:if>

<c:if test="${id == null}">
    <c:set var="id" value="${zfn:generateRandomDomID()}"/>
</c:if>


<c:if test="${numberOfEntities > 0 }">
    <c:choose>
        <c:when test="${numberOfEntities > maxNumber }">
            <span style="display:inline;" id="${id}-short">
            <c:forEach var="hyperlinkEntity" items="${entities}" varStatus="loop" end="${maxNumber -1}">
                <zfin:link entity="${hyperlinkEntity}"/><c:if test="${showAttributionLinks}"> <zfin:attribution
                    entity="${hyperlinkEntity.superterm}"/></c:if><c:if test="${!loop.last}">, </c:if>
            </c:forEach>
                <nobr>
                    (<a href="javascript:onClick=showEntityList('${id}', true)">all ${numberOfEntities}</a>)
                    <img onclick="showEntityList('${id}', true)" class="clickable"
                         src="/images/right_arrow.gif" alt="expand" title="Show all ${numberOfEntities} terms"/>
                </nobr>
                </span>
            <span style="display:none;" id="${id}-long">
            <c:choose>
                <c:when test="${useAjaxForLongVersion}">
                    <div id="fullList"></div>
                    <img onclick="showEntityList('${id}', false)" class="clickable"
                         src="/images/left_arrow.gif" alt="collapse" title="Show only first 5 terms">
                </c:when>
                <c:otherwise>
                    <c:forEach var="hyperlinkEntity" items="${entities}" varStatus="loop">
                        <zfin:link entity="${hyperlinkEntity}"/><c:if test="${showAttributionLinks}"> <zfin:attribution
                            entity="${hyperlinkEntity.superterm}"/></c:if><c:if test="${!loop.last}">, </c:if>
                    </c:forEach>&nbsp;
                    <img onclick="showEntityList('${id}', false)" class="clickable"
                         src="/images/left_arrow.gif" alt="collapse" title="Show only first 5 terms">
                </c:otherwise>
            </c:choose>
                    </span>
        </c:when>
        <c:otherwise>
            <c:forEach var="hyperlinkEntity" items="${entities}" varStatus="loop">
                <zfin:link entity="${hyperlinkEntity}"/><c:if test="${showAttributionLinks}"> <zfin:attribution
                    entity="${hyperlinkEntity.superterm}"/></c:if><c:if test="${!loop.last}">, </c:if>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</c:if>

<script type="text/javascript">

    constructLoaded = false;

    function showEntityList(element_id, displayLongVersion) {
        var shortElement = document.getElementById(element_id + '-short');
        var longElement = document.getElementById(element_id + '-long');
        if (displayLongVersion) {
            shortElement.style.display = "none";
            longElement.style.display = "inline";
            if (!constructLoaded) {
                jQuery('#fullList').load('${ajaxLink}');
                constructLoaded = true;
            }
        } else {
            shortElement.style.display = "inline";
            longElement.style.display = "none";
        }
    }
</script>
