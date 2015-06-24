<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin2:show-dirty-fish formBean="${formBean}" />

<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.aoTerm}"
                                       recordsExist="${formBean.mutantsExist}"
                                       anatomyStatistics="${formBean.anatomyStatisticsMutant}"
                                       structureSearchLink="/action/ontology/show-all-dirty-fish/${formBean.aoTerm.zdbID}"
                                       choicePattern="0# genotypes| 1# genotype| 2# genotypes"
                                       allRecordsAreDisplayed="${formBean.allGenotypesAreDisplayed}"
                                       totalRecordCount="${formBean.fishCount}"
                                       useWebdriverURL="false"
                                       substructureSearchLink="/action/ontology/show-all-phenotype-mutants-substructures/${formBean.aoTerm.zdbID}"/>
