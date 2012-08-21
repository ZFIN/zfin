<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<table border="0" width="100%">
    <tbody>
    <tr align="left">
        <td><b>All ${formBean.genotypeCount} Genotypes
            affecting</b>
            <zfin:link entity="${formBean.aoTerm}"/>
            <c:if test="${includingSubstructures}">
                <b>or Substructures</b>
            </c:if>
        </td>
    </tr>
    </tbody>
</table>

<zfin2:show-phenotype-mutants formBean="${formBean}" includingSubstructures="${includingSubstructures}"/>

<zfin2:pagination paginationBean="${formBean}"/>