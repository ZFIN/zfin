<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    Marker <b>${formBean.markerToDeleteViewString}</b> has been merged into
    <zfin:link entity="${formBean.markerToMergeInto}"/>
</z:page>