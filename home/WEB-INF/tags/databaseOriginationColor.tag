<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="origination" type="org.zfin.sequence.blast.Origination" rtexprvalue="true" required="true" %>

      <c:choose>
         <c:when test="${origination.type eq 'CURATED'}">
         <c:set var="color" value="green"/>
         </c:when>
         <c:when test="${origination.type eq 'GENERATED'}">
         <c:set var="color" value="gray"/>
         </c:when>
         <c:when test="${origination.type eq 'LOADED'}">
         <c:set var="color" value="blue"/>
         </c:when>
         <c:when test="${origination.type eq 'EXTERNAL'}">
         <c:set var="color" value="darkgoldenrod"/>
         </c:when>
         <c:otherwise>
         <c:set var="color" value="red"/>
         </c:otherwise>
      </c:choose>
      <font color="${color}">
      ${origination.type}
      </font>
