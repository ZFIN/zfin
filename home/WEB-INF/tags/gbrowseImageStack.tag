<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImage" type="org.zfin.gbrowse.presentation.GBrowseImage" required="true"
              description="List of GBrowseImage objects"%>
<%@ attribute name="width" required="false"%>
<%@ attribute name="domId"%>

<c:if test="${!empty gbrowseImage}">

<div style="text-align: center ;  min-width:200px; ">
        <zfin2:gbrowseImage gbrowseImage="${gbrowseImage}" width="${width}" domId="${domId}"/>
</div>
</c:if>
