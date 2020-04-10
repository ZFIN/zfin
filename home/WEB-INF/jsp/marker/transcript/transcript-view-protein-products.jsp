<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<zfin2:markerSummaryDBLinkDisplay marker="${formBean.marker}" links="${formBean.proteinProductDBLinkDisplay}"
                                  title="PROTEIN PRODUCTS"/>