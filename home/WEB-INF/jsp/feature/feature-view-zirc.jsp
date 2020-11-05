<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>


    <z:ifHasData test="${!empty formBean.ftrCommContr}" noDataMessage="None">
    <z:attributeList>

        <z:attributeListItem label="Genotyping protocol">
            ${formBean.ftrCommContr.functionalConsequence.toString()}
        </z:attributeListItem>



    </z:attributeList>
    </z:ifHasData>