<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h3 class="page-header">
            Updates for ${zdbID}
            <z:dataManagerDropdown>
                <a class="dropdown-item" href="/${zdbID}"><i class="fas fa-eye"></i> View</a>
            </z:dataManagerDropdown>
        </h3>
        <p class="lead">
            <a href="/${publication.zdbID}">${publication.title}</a>
            <c:if test="${!empty publication.fileName}"> <a
                    href="${ZfinPropertiesEnum.PDF_LOAD.value()}/${publication.fileName}" target="_blank"><i
                    class="far fa-file-pdf"></i></a></c:if>
        </p>
        <div class="__react-root" id="EntityUpdatesTable" data-entity-id="${zdbID}" data-field-name-filter="${fieldNameFilter}"></div>
    </div>
    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>