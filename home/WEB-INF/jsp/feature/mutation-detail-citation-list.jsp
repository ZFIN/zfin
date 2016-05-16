<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.MutationDetailAttributionList" scope="request"/>

<zfin2:citationList pubListBean="${formBean}"
                    url="/action/feature/${formBean.feature.zdbID}/mutation-detail-citations?type=${formBean.type.toString()}&">
    <table class="primary-entity-attributes">
        <tr>
            <th class="genotype-name-label">
                <span class="name-label">Genomic Feature:</span>
            </th>
            <td>
                <zfin:link entity="${formBean.feature}"/>
            </td>
        </tr>
    </table>
</zfin2:citationList>
