<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid">
    <div class="__react-root"
         id="IndexingBin"
         data-user-id="${currentUser.zdbID}"
         data-current-status="${currentStatus.id}"
         data-next-status="${nextStatus.id}"
    >
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
