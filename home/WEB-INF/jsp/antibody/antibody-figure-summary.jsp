<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodyBean" scope="request"/>

<div class="data-sub-page-title">Antibody Expression Figure Summary</div>

<div style="float: right; margin: .2em;">
<tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
    <tiles:putAttribute name="subjectName" value="${formBean.antibody.name}"/>
    <tiles:putAttribute name="subjectID" value="${formBean.antibody.zdbID}"/>
</tiles:insertTemplate>
</div>

<zfin2:expressionSummaryCriteria criteria="${formBean.expressionSummaryCriteria}"/>

<div class="summary">
  <span class="summaryTitle">Antibody Labeling</span>

    <%-- todo: need a class name for this --%>
    (${formBean.antibodyStat.numberOfFiguresDisplay} from ${formBean.antibodyStat.numberOfPublicationsDisplay})
    <span style="float: right">

        <c:choose>
            <c:when test="${formBean.onlyFiguresWithImg}">
                [ <a
                    href="javascript:document.location.replace('antibody-figure-summary?antibodyID=${formBean.antibody.zdbID}&superTermID=${formBean.superTerm.zdbID}&subTermID=${formBean.subTerm.zdbID}<c:if test="${formBean.startStage.zdbID != null}">&startStageID=${formBean.startStage.zdbID}&endStageID=${formBean.endStage.zdbID}</c:if>&figuresWithImg=false')">
                Show all figures</a> ]
            </c:when>
            <c:otherwise>
                [ <a
                    href="javascript:document.location.replace('antibody-figure-summary?antibodyID=${formBean.antibody.zdbID}&superTermID=${formBean.superTerm.zdbID}&subTermID=${formBean.subTerm.zdbID}<c:if test="${formBean.startStage.zdbID != null}">&startStageID=${formBean.startStage.zdbID}&endStageID=${formBean.endStage.zdbID}</c:if>&figuresWithImg=true')">
                Show only figures with images</a> ]
            </c:otherwise>
        </c:choose>
    </span>
    <zfin2:figureSummary figureSummaryDisplayList="${formBean.antibodyStat.figureSummary}" expressionData="true"/>
</div>
