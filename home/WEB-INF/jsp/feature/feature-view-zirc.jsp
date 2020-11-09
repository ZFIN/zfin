<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>


    <z:ifHasData test="${!empty formBean.zircGenoLink}" noDataMessage="None">
    <z:attributeList>

        <z:attributeListItem label="Genotyping protocol">
            <a href="/zebrafish.org/fish/pdf/pcr/${formBean.zircGenoLink.accessionNumberDisplay}">${formBean.zircGenoLink.accessionNumberDisplay}</a>

        </z:attributeListItem>



    </z:attributeList>
    </z:ifHasData>