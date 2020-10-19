<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <zfin2:listAllFromOrganization/>

    <zfin2:personList letter="${letter}" people="${people}"/>
</z:page>