<%@ attribute name="sequenceInfo" type="org.zfin.marker.presentation.SequenceInfo" rtexprvalue="true" required="true" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>

<c:if test="${fn:length(sequenceInfo.proteinProducts) > 0}">
    <table class="summary">
    <caption>Protein Products</caption>
    <tr><td>
        <c:forEach var="proteinProduct" items="${sequenceInfo.proteinProducts}" varStatus="index">
            <zfin:link entity="${proteinProduct}"/> <zfin:attribution entity="${proteinProduct}"/>
            <c:if test="!index.last">,</c:if>
        </c:forEach>
    </td></tr>
    </table>
</c:if>
