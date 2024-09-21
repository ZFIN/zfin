<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h3 class="page-header">Updates for ${zdbID}</h3>
        <div class="__react-root" id="EntityUpdatesTable" data-entity-id="${zdbID}"></div>
    </div>
    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>