<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<zfin2:inputWelcome marker="${formBean.marker}"/>

<zfin2:sequenceHead gene="${formBean.marker}"/>


<%--SEQUENCE INFORMATION--%>
<zfin2:transcriptSequenceInformation sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="true"/>

<%-- ENCODES relationships --%>
<zfin2:markerRelationships relationships="${formBean.markerRelationships}" marker="${formBean.marker}"
                           title="${fn:toUpperCase('Segment (Clone and Probe) Relationships')}" />



<%--<zfin2:markerSummaryPages links="${formBean.otherLinks}" marker="${formBean.marker}"/>--%>