<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <zfin2:listAllFromOrganization />

    <zfin2:organizationList type="${type}" organizations="${orgs}"/>
</z:page>
