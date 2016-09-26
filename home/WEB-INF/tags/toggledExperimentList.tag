<%--
  This tag creates a list of hyperlink for a list of conditions.
  At a minimum, they must be supported by CreateExperimentTag.java,
  to do attribution as well.

  If the list is bigger than a given maximum number we only display
  the max number of terms, add the total number and offer a link to expand to
  see the total list.

  Parameters:
    expressionResults: the object that holds all expression result objects
    maxNumber:  the max number of terms to display for the short version
    id:   A unique span ID, should be the zdbID of the entity being listed.

 --%>

<%@ tag body-content="scriptless" %>
<%@attribute name="experimentList" type="java.util.Collection" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@attribute name="id" type="java.lang.String" required="false" %>
<%@attribute name="showAttributionLinks" type="java.lang.Boolean" required="false" %>
<%@attribute name="suppressPopupLinks" type="java.lang.Boolean" required="false" %>
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


<c:if test="${fn:length(experimentList) > 0 }">
    <c:choose>
        <c:when test="${fn:length(experimentList) > maxNumber }">
            <span style="display:inline;" id="${id}-short">
            <c:forEach var="experiment" items="${experimentList}" varStatus="loop" end="${maxNumber -1}">
                <zfin:experiment experiment="${experiment}"/><c:if test="${showAttributionLinks}"> </c:if><c:if
                    test="${!loop.last}">, </c:if>
            </c:forEach>
                <nobr>
                    (<a href="javascript:onClick=showEntityList('${id}', true)">all ${fn:length(experimentList)}</a>)
                    <img onclick="showEntityList('${id}', true)" class="clickable"
                         src="/images/right_arrow.gif" alt="expand" title="Show all ${fn:length(experimentList)} terms">
                </nobr>
                </span>
            <span style="display:none;" id="${id}-long">
            <c:forEach var="experiment" items="${experimentList}" varStatus="loop">
                <zfin:experiment experiment="${experiment}"/><c:if test="${showAttributionLinks}"> </c:if><c:if
                    test="${!loop.last}">, </c:if>
            </c:forEach>&nbsp;
                <img onclick="showEntityList('${id}', false)" class="clickable"
                     src="/images/left_arrow.gif" alt="collapse" title="Show only first ${maxNumber+1} terms">
                </span>
        </c:when>
        <c:otherwise>
            <c:forEach var="experiment" items="${experimentList}" varStatus="loop">
                <zfin:experiment experiment="${experiment}"/><c:if test="${showAttributionLinks}"> <zfin:attribution
                    entity="${experiment.superterm}"/></c:if><c:if test="${!loop.last}">, </c:if>
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
