<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="fish" class="org.zfin.mutant.Fish" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID" copyable="true">
        ${fish.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${fish}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Genotype">
        <zfin:link entity="${fish.genotype}"/>
        <c:if test="${fish.genotype.extinct}">
            &nbsp;&nbsp; <i class="warning-icon" title="Extinct"></i>&nbsp;
        </c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Targeting Reagent">
        <zfin:link entity="${fish.strList}"/>
    </z:attributeListItem>

</z:attributeList>