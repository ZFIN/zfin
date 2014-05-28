<%@ page import="org.zfin.framework.presentation.ZfinStatisticsBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="90%">

    <tr><td colspan="3" class="sectionTitle">Zfin Statistics </td></tr>
    <tr>
        <td class="sectionTitle">APG Files</td>
        <td width="120" colspan="2" class="sectionTitle"></td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Number of APG files [Total]
        </td>
        <td colspan="2" class="listContentBold">
            <c:out value="${formBean.numberOfApgFiles}"/> [<%= ZfinStatisticsBean.TOTAL_NUMBER_OF_FILES %>]
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Relative Number of APG file.
        </td>
        <td colspan="2" class="listContentBold">
            <c:out value="${formBean.relativeNumberOfApgFiles}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Size of all APG files [Total]
        </td>
        <td colspan="2" class="listContentBold">
            <fmt:formatNumber value="${formBean.totalApgFileSize}" type="number" /> Bytes
            [<%= ZfinStatisticsBean.TOTAL_SIZE %>]
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Relative Size of APG files
        </td>
        <td colspan="2" class="listContentBold">
            <c:out value="${formBean.relativeApgFileSize}"/>
        </td>
    </tr>
    <tr>
        <td class="sectionTitle">JSP Files</td>
        <td width="120" colspan="2" class="sectionTitle"></td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Number of JSP files
        </td>
        <td colspan="2" class="listContentBold">
            <c:out value="${formBean.numberOfJspFiles}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Size of all JSP files [Bytes]
        </td>
        <td colspan="2" class="listContentBold">
            <fmt:formatNumber value="${formBean.totalJspFileSize}" type="number" />
        </td>
    </tr>
    <tr>
        <td class="sectionTitle">Java Classes</td>
        <td width="120" colspan="2" class="sectionTitle"></td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Number of Java Class files
        </td>
        <td colspan="2" class="listContentBold">
            <c:out value="${formBean.numberOfClassFiles}"/>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Size of all Java class files [Bytes]
        </td>
        <td colspan="2" class="listContentBold">
            <fmt:formatNumber value="${formBean.totalClassesFileSize}" type="number" />
        </td>
    </tr>
</table>
