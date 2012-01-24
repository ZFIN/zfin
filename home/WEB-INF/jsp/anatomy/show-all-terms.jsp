<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<CENTER>
    <TABLE width="98%">
        <TR>
            <TD>
                All Anatomical Terms in alphabetical order:
                <HR width=500 size=1 noshade align=left>
                <tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_list.jsp" flush="false"/>
            </TD>
        </TR>
    </TABLE>
</CENTER>

