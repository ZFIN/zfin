<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="strBean" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.DisruptorBean" %>
<%@ attribute name="webdriverRoot" required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="GENOTYPES CREATED WITH "/>
</c:if>

<a name="genotype"></a>
<div class="summary">
    <span class="summaryTitle">${title}<zfin:name entity="${strBean.marker}"/>
       <c:if test="${empty strBean.genotypes}">
           <span class="no-data-tag">No data available</span>
       </c:if>
    </span>
    <c:if test="${!empty strBean.genotypes}">
        <c:forEach var="genotype" items="${strBean.genotypes}" varStatus="loop">
            <zfin:link entity="${genotype}"/><c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:forEach>
    </c:if>
</div>

