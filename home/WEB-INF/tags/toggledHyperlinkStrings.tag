<%--
Similar to toggledHypelinkList, but the collection is a collection of strings that are displayed directly.
Also, we provide a suffix so that we can change the display easily.
--%>

<%@ tag body-content="scriptless" %>
<%@attribute name="collection" type="java.util.Collection" %>
<%@attribute name="maxNumber" type="java.lang.Integer" %>
<%@attribute name="id" type="java.lang.String" %>
<%@ attribute name="suffix" type="java.lang.String" required="false" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${empty suffix}">
    <c:set var="suffix" value=", "/>
</c:if>

<c:if test="${fn:length(collection) > 0 }">
    <c:choose>
        <c:when test="${fn:length(collection) > maxNumber }">

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

            <span style="display:inline;" id="${id}-short">
            <c:forEach var="hyperlinkEntity" items="${collection}" varStatus="loop" end="${maxNumber -1}">
                ${hyperlinkEntity}${(!loop.last ? suffix : "")}
            </c:forEach>
                <nobr>
                    (<a href="javascript:onClick=showEntityList('${id}', true)">all ${fn:length(collection)}</a>)
                    <img onclick="showEntityList('${id}', true)" class="clickable"
                         src="/images/right_arrow.gif" alt="expand" title="Show all ${fn:length(collection)} terms">
                </nobr>
                </span>
            <span style="display:none;" id="${id}-long">
            <c:forEach var="hyperlinkEntity" items="${collection}" varStatus="loop">
                ${hyperlinkEntity}${(!loop.last ? suffix : "")}
            </c:forEach>&nbsp;
                <img onclick="showEntityList('${id}', false)"  class="clickable"
                     src="/images/left_arrow.gif" alt="collapse" title="Show only first ${maxNumber+1} terms">
                </span>
        </c:when>
        <c:otherwise>
            <c:forEach var="hyperlinkEntity" items="${collection}" varStatus="loop">
                ${hyperlinkEntity}${(!loop.last ? suffix : "")}
            </c:forEach>
        </c:otherwise>
    </c:choose>
</c:if>

