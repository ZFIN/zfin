<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

    <div class="container-fluid">
        <div class="__react-root" id="PubDashboard" data-user-id="${currentUser.zdbID}"></div>
    </div>

    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>