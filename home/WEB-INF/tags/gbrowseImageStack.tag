<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImages" type="java.util.List" required="true"
              description="List of GBrowseImage objects"%>
<%@ attribute name="width" required="false"%>

<c:if test="${!empty gbrowseImages}">

<div style="text-align: center ;  min-width:200px; "> 
    <c:forEach items="${gbrowseImages}" var="image">
        <zfin2:gbrowseImage gbrowseImage="${image}" width="${width}"/>
    </c:forEach>

</div>
</c:if>
