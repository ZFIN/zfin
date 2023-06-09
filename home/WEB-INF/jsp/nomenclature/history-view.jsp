<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>

    <div class="__react-root"
         id="NomenclatureEdit"
         data-marker-id="${markerZdbID}"
         data-has-root="<authz:authorize access="hasRole('root')">true</authz:authorize>"
    ></div>

    <script src="${zfn:getAssetPath("react.js")}"></script>

</z:page>
