<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--Adds the GOEditController.--%>
<c:set var="pubID" value="${param.zdbID}" />
<c:if test="${empty pubID}">
    <c:set var="pubID" value="ZDB-PUB-080701-3" />
</c:if>

<z:devtoolsPage>

    <script type="text/javascript">
        var curationProperties = {
            zdbID : "${pubID}",
            moduleType: "ENVIRONMENT_CURATION",
            debug: "false"
        }
    </script>

    <script language="javascript" src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

    <%--define the lookups up here--%>

    <div id="${StandardDivNames.directAttributionDiv}"></div>


    <%--</authz:authorize>--%>
</z:devtoolsPage>


