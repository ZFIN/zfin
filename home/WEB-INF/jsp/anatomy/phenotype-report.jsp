<%@ page import="org.zfin.framework.presentation.PaginationBean" %>
<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="6" align="right"><a href="#modify-search">Modify Search</a></td>
    </tr>
    <tr>
        <td>
            <div align="center">
                <c:choose>
                    <c:when test="${formBean.totalRecords == 0}">
                        <b>No Phenotype records were found matching your query.</b><br><br>
                    </c:when>
                    <c:otherwise>
                        <b>
                            <zfin:choice choicePattern="0#Phenotypes| 1#Phenotype| 2#Phenotypes"
                                         integerEntity="${formBean.totalRecords}" includeNumber="true"/>
                        </b>
                    </c:otherwise>
                </c:choose>
            </div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
</table>

<c:if test="${formBean.totalRecords > 0}">
    <table class="searchresults rowstripes">
        <tr>
            <th width=20%>Genotype</th>
            <th width=20%>Morpholino</th>
            <th width=30%>Entity</th>
            <th width=30%>Quality</th>
        </tr>
        <c:forEach var="phenotype" items="${formBean.allPhenotype}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${phenotype.phenotypeExperiment.genotypeExperiment.genotype}"/>
                </td>
                <td>
                    <c:if test="${phenotype.phenotypeExperiment.genotypeExperiment.experiment.experimentConditions ne null}">
                        <c:forEach var="morpholino"
                                   items="${phenotype.phenotypeExperiment.genotypeExperiment.experiment.experimentConditions}">
                            ${morpholino.morpholino.abbreviation},
                        </c:forEach>
                    </c:if>
                </td>
                <td>
                    <zfin:link entity="${phenotype}"/>
                </td>
                <td>
                    <zfin:link entity="${phenotype.quality}"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

    <input name="page" type="hidden" value="1" id="page"/>
    <zfin2:pagination paginationBean="${formBean}"/>
</c:if>
<p></p>
<table width="100%">
    <tr>
        <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        <a name="modify-search">Modify your search </a>
            </span>
            &nbsp;&nbsp; <a href="javascript:start_tips();">Search Tips</a>
        </td>
    </tr>
</table>

<table width="100%" class="error-box">
    <tr>
        <td>
            <form:errors path="*" cssClass="Error"/>
        </td>
    </tr>
</table>

<zfin-ontology:phenotype-report-form formBean="${formBean}"/>
