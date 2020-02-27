<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="title">
    <tiles:getAsString ignore="true" name="staticTitle"/> ${dynamicTitle}
</c:set>

<tiles:useAttribute name="bodyClass" ignore="true"/>

<z:page title="${title}" bodyClass="${bodyClass}">
    <tiles:insertAttribute name="header" ignore="true" />
    <tiles:insertAttribute name="body" />
</z:page>
