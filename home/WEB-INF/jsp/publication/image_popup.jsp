<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Strict//EN"> 
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div id="figpop_top">
<a class="close_link" href="javascript://close popup;"
   onClick="parent.Control.Modal.close();">x</a>
  <strong>
    ${formBean.image.figure.publication.shortAuthorList}
    &nbsp;&nbsp;
    <zfin:link entity="${formBean.image.figure}"/>
  </strong>
  <c:if test="${fn:length(formBean.image.figure.images) > 1 }">
      &nbsp; (Contains ${fn:length(formBean.image.figure.images)} images) 
  </c:if>
</div>


&nbsp;
<c:if test="${!empty formBean.expressionGenes}">
  <c:if test="${fn:length(formBean.expressionGenes) > 1 }">All ${formBean.image.figure.label} genes: </c:if>
  <c:if test="${fn:length(formBean.expressionGenes) == 1 }">Gene: </c:if>
  <c:forEach var="probeStats" items="${formBean.expressionGenes}" varStatus="loop">
      <zfin:link entity="${probeStats}" /><c:if test="${!loop.last}">,</c:if>
  </c:forEach>
</c:if>



<div id="figpop_image">
   <zfin:link entity="${formBean.image}">
     <img src="${formBean.mediumImageURL}" />
   </zfin:link>
</div>



<script type="text/javascript">
        var links = document.getElementsByTagName("a");
        for (var i=0; i < links.length; i++ ) {
            links[i].target="_top";
        }
</script>




