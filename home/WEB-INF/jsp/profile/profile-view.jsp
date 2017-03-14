<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:choose>
    <c:when test="${fn:startsWith(formBean.zdbID,'ZDB-PERS')}">
        <zfin2:personView person="${formBean}" companies="${company}" labs="${labs}"
                          editURL="/action/profile/person/edit/${formBean.zdbID}"
                          deleteURL="${deleteURL}" isOwner="${isOwner}"
                />
    </c:when>
    <c:when test="${fn:startsWith(formBean.zdbID,'ZDB-COMPANY')}">
        <zfin2:companyView company="${formBean}" publications="${publications}" members="${members}" prefixes="${prefixes}"
                           editURL="/action/profile/company/edit/${formBean.zdbID}"
                           deleteURL="${deleteURL}" isOwner="${isOwner}"
                />
    </c:when>
    <c:when test="${fn:startsWith(formBean.zdbID,'ZDB-LAB')}">
        <zfin2:labView lab="${formBean}" publications="${publications}" members="${members}" prefixes="${prefixes}"
                       editURL="/action/profile/lab/edit/${formBean.zdbID}"
                       deleteURL="${deleteURL}" isOwner="${isOwner}"
                       hasCoPi="${hasCoPi}" noPrefixes="${noPrefixes}" numOfFeatures="${numOfFeatures}"
                />
    </c:when>
</c:choose>





