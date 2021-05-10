<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<z:page>
    <table border="0" width="100%">
        <tbody>
        <tr align="left">
            <td><b>${formBean.fishCount} Fish
                affecting</b>
                <zfin:link entity="${formBean.aoTerm}"/>
                <c:if test="${includingSubstructures}">
                    or subterms
                </c:if>
            </td>
        </tr>
        </tbody>
    </table>

    <zfin2:show-phenotype-mutants formBean="${formBean}" includingSubstructures="${includingSubstructures}"/>

    <zfin2:pagination paginationBean="${formBean}"/>
</z:page>