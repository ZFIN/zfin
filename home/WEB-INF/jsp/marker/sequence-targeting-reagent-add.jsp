<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentAddBean" scope="request"/>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h2 class="page-header">New Sequence Targeting Reagent</h2>
        <div class="__react-root"
             id="NewSequenceTargetingReagentForm"
             data-pub-id="${formBean.publicationID}"
             data-str-type="${formBean.strType}"
             data-str-types-json="${strTypesJson}"
             data-field-errors-json="${fieldErrorsJson}"
        >
        </div>
    </div>

    <script src="${zfn:getAssetPath("react.js")}"></script>

</z:page>

