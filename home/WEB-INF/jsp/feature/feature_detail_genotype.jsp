<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%--TODO: remove?--%>
<b>Mutant and Transgenic Lines</b>


    <table width="100%">
        <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Genotype (Background)
            </TD>
            <TD width="20%">
                Affected Genes
            </TD>
            <TD width="20%">
                Phenotype
            </TD>
            <TD width="40%">
                Figures
            </TD>
        </TR>
        <c:forEach var="featgenoStat" items="${formBean.featgenoStats}">
            <tr class="search-result-table-entries">
                <td>
                    <zfin:link entity="${featgenoStat.genotype}"/>
                </td>
                <td>
                    <zfin:link entity="${featgenoStat.affectedMarkers}"/>
                </td>
                <td>
                </td>
                <td>
        </c:forEach>
        </tbody>
    </table>
