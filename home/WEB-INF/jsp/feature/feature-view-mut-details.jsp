<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.FeatureBean" scope="request"/>

<zfin2:mutationDetailsAttributeList feature="${formBean.feature}"
                                    mutationDetails="${formBean.mutationDetails}"
                                    dnaChangeAttributions="${formBean.dnaChangeAttributions}"
                                    transcriptConsequenceAttributions="${formBean.transcriptConsequenceAttributions}"
                                    proteinConsequenceAttributions="${formBean.proteinConsequenceAttributions}"/>
