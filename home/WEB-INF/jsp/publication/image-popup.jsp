<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Strict//EN"> 
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="figpop_top">
  <strong>
    ${formBean.image.figure.publication.shortAuthorList}
    &nbsp;
    <zfin:link entity="${formBean.image.figure}"/>
  </strong>
  <c:if test="${fn:length(formBean.image.figure.images) > 1 }">
      (Contains ${fn:length(formBean.image.figure.images)} images)
  </c:if>
</div>

<c:if test="${!empty formBean.expressionGenes}">
  <c:if test="${fn:length(formBean.expressionGenes) > 1 }">All ${formBean.image.figure.label} genes: </c:if>
  <c:if test="${fn:length(formBean.expressionGenes) == 1 }">Gene: </c:if>
  <span class="gene-links">
    <c:forEach var="probeStats" items="${formBean.expressionGenes}" varStatus="loop">
        <zfin:link entity="${probeStats}" /><c:if test="${!loop.last}">, </c:if>
    </c:forEach>
  </span>
</c:if>

<div class="figpop_image">
   <zfin:link entity="${formBean.image}">
       <img src="${formBean.mediumImageURL}" />
   </zfin:link>
</div>
