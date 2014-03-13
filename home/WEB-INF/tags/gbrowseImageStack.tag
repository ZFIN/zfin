<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImages" type="org.zfin.gbrowse.presentation.GBrowseImage" required="true"
              description="List of GBrowseImage objects"%>
<%@ attribute name="width" required="false"%>

<c:if test="${!empty gbrowseImages}">

<div style="text-align: center ;  min-width:200px; ">
        <zfin2:gbrowseImage gbrowseImage="${image}" width="${width}"/>
</div>
</c:if>
