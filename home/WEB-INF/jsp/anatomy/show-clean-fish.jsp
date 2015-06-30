<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin2:show-phenotype-mutants formBean="${formBean}" />

<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.aoTerm}"
                                       recordsExist="${formBean.mutantsExist}"
                                       anatomyStatistics="${formBean.anatomyStatisticsMutant}"
                                       structureSearchLink="/action/ontology/show-all-clean-fish/${formBean.aoTerm.zdbID}"
                                       choicePattern="0# Fish| 1# Fish| 2# Fish"
                                       allRecordsAreDisplayed="${formBean.allGenotypesAreDisplayed}"
                                       totalRecordCount="${formBean.fishCount}"
                                       useWebdriverURL="false"
                                       substructureSearchLink="/action/ontology/show-all-clean-fish-include-substructures/${formBean.aoTerm.zdbID}"/>
