<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin2:show-phenotype-mutants formBean="${formBean}" />

<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.aoTerm}"
                                       recordsExist="${formBean.mutantsExist}"
                                       anatomyStatistics="${formBean.anatomyStatisticsMutant}"
                                       structureSearchLink="/action/anatomy/show-all-phenotype-mutants/${formBean.aoTerm.zdbID}"
                                       choicePattern="0# genotypes| 1# genotype| 2# genotypes"
                                       allRecordsAreDisplayed="${formBean.allGenotypesAreDisplayed}"
                                       totalRecordCount="${formBean.genotypeCount}"
                                       useWebdriverURL="false"/>
