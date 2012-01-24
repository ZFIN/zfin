<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        Go Evidence Report
            </span>
        </td>
    </tr>
</table>

<zfin-ontology:go-evidence-report-form formBean="${formBean}"/>