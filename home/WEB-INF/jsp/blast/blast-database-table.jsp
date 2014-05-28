<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--Based on blast_info.html and trancript_types--%>
<style type="text/css">
    div.summary li { padding-top: .1em; padding-bottom: .1em; }
</style>

<div class="summary">
    <c:if test="${!empty formBean.nucleotideDatabases}">
        <table width="100%">
            <tr>
                <td colspan="2">
                    <c:if test="${formBean.showTitle}"> <h3>Nucleotide Blast Databases</h3></c:if>
                </td>
                <td align="right">
                    <a href="/action/blast/blast-definitions?doRefresh=true">[refresh]</a>
                </td>
            </tr>
        </table>
        <table>
            <tr>
                <th>name (abbrev)</th>
                <th>count (acc/blast)</th>
                <th>definition</th>
                <th>public</th>
                <th>origination</th>
            </tr>
            <c:forEach var="database" items="${formBean.nucleotideDatabases}">
                <tr>
                    <zfin2:blastDatabaseTable database="${database}"/>
                </tr>
            </c:forEach>
        </table>
    </c:if>
    <c:if test="${!empty formBean.proteinDatabases}">
        <c:if test="${formBean.showTitle}"> <h3>Protein Blast Databases</h3></c:if>
        <table>
            <c:forEach var="database" items="${formBean.proteinDatabases}">
                <tr>
                    <zfin2:blastDatabaseTable database="${database}"/>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</div>



