<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="title">
    <tiles:getAsString ignore="true" name="staticTitle"/> ${dynamicTitle}
</c:set>

<zfin2:page title="${title}">
    <tiles:insertAttribute name="header" ignore="true" />
    <tiles:insertAttribute name="body" />
</zfin2:page>
