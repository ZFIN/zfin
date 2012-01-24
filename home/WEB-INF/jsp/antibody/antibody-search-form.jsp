<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodySearchFormBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        Search for Antibodies
            </span>

            <a href="/ZFIN/misc_html/antibody_search_tips.html" class="popup-link help-popup-link"></a>

        </td>
        <td align="right" class="titlebar">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Antibody search"/>
                <tiles:putAttribute name="subjectID" value=""/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>

<zfin-marker:antibody-search-form formBean="${formBean}"/>

