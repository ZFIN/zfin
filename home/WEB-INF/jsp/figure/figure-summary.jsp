<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-figure:expressionSummary genes="${expressionGenes}"
                               antibodies="${expressionAntibodies}"
                               fish="${expressionFish}"
                               strs="${expressionSTRs}"
                               experiments="${expressionConditions}"
                               entities="${expressionEntities}"
                               start="${expressionStartStage}" end="${expressionEndStage}"
                               probe="${probe}" probeSuppliers="${probeSuppliers}"/>

<zfin-figure:phenotypeSummary fish="${phenotypeFish}"
                              strs="${phenotypeSTRs}"
                              entities="${phenotypeEntities}"
                              experiments="${phenotypeConditions}"
                              start="${phenotypeStartStage}" end="${phenotypeEndStage}"/>