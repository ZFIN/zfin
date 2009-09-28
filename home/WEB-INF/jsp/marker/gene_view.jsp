<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
    <tiles:put name="subjectName" value="${formBean.gene.name}"/>
    <tiles:put name="subjectID" value="${formBean.gene.zdbID}"/>
</tiles:insert>

<zfin2:geneHead gene="${formBean.gene}"/>

<%--
<zfin2:markerSequenceInformation sequenceInfo="${formBean.sequenceLinks}" />
--%>

<zfin2:markerSummaryPages links="${formBean.otherLinks}" marker="${formBean.gene}"/>