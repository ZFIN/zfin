<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<table border="0" width="100%">
    <tbody>
    <tr align="left">
        <td><b>All ${formBean.genotypeCount} Genotypes
            for:</b>
            <zfin:link entity="${formBean.aoTerm}"/>
        </td>
    </tr>
    </tbody>
</table>

<zfin2:show-phenotype-mutants formBean="${formBean}"/>

<zfin2:pagination paginationBean="${formBean}"/>