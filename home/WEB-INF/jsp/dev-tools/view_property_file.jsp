<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="50%">
    <tr>
        <td colspan=2 class="sectionTitle"><c:out value="${fileWrapper.title}" /></td>
    </tr>

    <tr>
        <td class="listContent" width="120">
            File Name
        </td>
        <td class="listContent">
            <c:out value="${fileWrapper.fileName}" />
        </td>
    </tr>
    <tr>
        <td class="listContent" width="120">
            File Path
        </td>
        <td class="listContent">
            <c:out value="${fileWrapper.file.absolutePath}" />
        </td>
    </tr>
    <tr>
        <td class="listContent" width="120">
            Contents
        </td>
        <td class="listContent">
            <pre>
            <c:out value="${fileWrapper.contents}" />
                </pre>
        </td>
    </tr>
    </table>
