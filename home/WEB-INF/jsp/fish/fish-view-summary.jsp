<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="fish" class="org.zfin.mutant.Fish" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${fish.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${fish}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Genotype">
        <zfin:link entity="${fish.genotype}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Targeting Reagent">
        <zfin:link entity="${fish.strList}"/>
    </z:attributeListItem>

</z:attributeList>