<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <jsp:include page="/WEB-INF/jsp/feature/line-designation-description.jsp"/>
    <%--<jsp:include page="/WEB-INF/jsp/feature/line-designation-add.jsp"/>--%>

    <zfin2:lineDesignation lineDesignationBean="${formBean}"/>
</z:page>
