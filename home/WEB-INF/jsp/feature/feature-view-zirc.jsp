<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>



    <z:attributeList>

        <z:attributeListItem label="Genotyping protocol">
            <z:ifHasData test="${!empty formBean.zircGenoLink}" noDataMessage="None">
            <a href="zebrafish.org/fish/pdf/pcr/${formBean.zircGenoLink.accessionNumberDisplay}">${formBean.zircGenoLink.accessionNumberDisplay}</a>
            </z:ifHasData>
        </z:attributeListItem>



    </z:attributeList>
