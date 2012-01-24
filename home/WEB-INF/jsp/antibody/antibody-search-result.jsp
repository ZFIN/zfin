<%@ page import="org.zfin.antibody.AntibodyType" %>
<%@ page import="org.zfin.antibody.presentation.AntibodySearchFormBean" %>
<%@ page import="org.zfin.framework.presentation.PaginationBean" %>
<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.util.FilterType" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodySearchFormBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="6" align="right"><a href="#modify-search">Modify Search</a></td>
    </tr>
    <tr>
        <td>
            <div align="center">
                <c:choose>
                    <c:when test="${formBean.totalRecords == 0}">
                        <b>No antibodies were found matching your query.</b><br><br>
                    </c:when>
                    <c:otherwise>
                        <b>
                            <zfin:choice choicePattern="0#Antibodies| 1#Antibody| 2#Antibodies"
                                         integerEntity="${formBean.totalRecords}" includeNumber="true"/>
                        </b>
                    </c:otherwise>
                </c:choose>
            </div>
        </td>
        <td align="right" width="80">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Antibody search"/>
                <tiles:putAttribute name="subjectID" value=""/>
            </tiles:insertTemplate>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
</table>

<zfin-marker:antibody-search-result formBean="${formBean}" />
<zfin-marker:antibody-search-form formBean="${formBean}" />


