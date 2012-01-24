<%@ page import="org.zfin.antibody.AntibodyType" %>
<%@ page import="org.zfin.antibody.presentation.AntibodySearchFormBean" %>
<%@ page import="org.zfin.util.FilterType" %>
<%@ page import="org.zfin.framework.presentation.PaginationBean" %>
<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.gwt.lookup.ui.Lookup" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" scope="request"/>

        <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
                <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        Expression  Report
            </span>
                </td>
            </tr>
        </table>

<zfin-ontology:expression-report-form formBean="${formBean}"/>