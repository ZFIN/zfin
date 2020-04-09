<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}"
                                        title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>