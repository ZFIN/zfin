<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page title="ZFIN: Processing BLAST: ${dynamicTitle}">
    <h3>Executing search ...</h3>

    <zfin2:blastProcessing formBean="${formBean}"/>
</z:page>