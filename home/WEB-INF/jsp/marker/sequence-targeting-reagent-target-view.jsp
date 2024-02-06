<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:page>
    <div class="summaryTitle">Target Locations for <zfin:link entity="${formBean.marker}"/></div>
    <style>
        .jbrowse-container {
            margin-bottom: 40px;
            background-color: #f9f9f9;
            padding: 10px;
            padding-top: 5px;
        }
    </style>
    <c:forEach items="${formBean.gbrowseImages}" var="image" varStatus="loop">
        <div class="jbrowse-container">
        <h2>Location ${loop.index + 1}</h2>
        <zfin-gbrowse:genomeBrowserImageComponent image="${image}" loopIndex="${loop.index}" />
        </div>
    </c:forEach>

    <script src="${zfn:getAssetPath("react.js")}"></script>

</z:page>