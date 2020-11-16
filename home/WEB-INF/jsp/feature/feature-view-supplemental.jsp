<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>


    <z:ifHasData test="${!empty formBean.ftrCommContr}" noDataMessage="None">
    <z:attributeList>

        <z:attributeListItem label="Functional Consequence">
            ${formBean.ftrCommContr.functionalConsequence.toString()}
        </z:attributeListItem>

        <z:attributeListItem label="Adult Viable">
            <zfin2:nullableBoolean value="${formBean.ftrCommContr.adultViable}"/>
        </z:attributeListItem>

        <z:attributeListItem label="Maternal Zygosity Examined">
            <zfin2:nullableBoolean value="${formBean.ftrCommContr.maternalZygosityExamined}"/>
        </z:attributeListItem>

        <z:attributeListItem label="NMD Apparent">
            ${formBean.ftrCommContr.nmdApparent.toString()}
        </z:attributeListItem>
        <z:attributeListItem label="Available">
            <zfin2:nullableBoolean value="${formBean.ftrCommContr.currentlyAvailable}"/>
        </z:attributeListItem>

        <z:attributeListItem label="Other Line Information">
            ${formBean.ftrCommContr.otherLineInformation}
        </z:attributeListItem>


    </z:attributeList>
    </z:ifHasData>