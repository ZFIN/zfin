<%@ page import="org.zfin.orthology.Species" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<form:form method="POST">
<table width="100%">
    <tr>
        <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                Orthology Search
            </span>
            &nbsp;&nbsp;
        </td>
    </tr>
</table>

<table width="100%" class="error-box">
    <tr>
        <td>
            <form:errors path="*" cssClass="Error"/>
        </td>
    </tr>
</table>

<table width="100%">
<tr>
<td>
<table border="0" cellpadding="5" cellspacing="0" width="100%">
<tr>
    <td>
    </td>
    <td>
        <dt><b>Gene Symbol</b>:</dt>
    </td>
    <td>
        <dt><b>Chromosome(s)</b>:</dt>
    </td>
    <td>
        &nbsp;
    </td>
</tr>
<tr>
    <td>
        <dd>
            <%= Species.ZEBRAFISH.toString()%> (Danio rerio)
        </dd>
    </td>
    <td>
        <form:label path="zebrafishCriteriaBean.geneSymbolFilterType"/>
        <form:select path="zebrafishCriteriaBean.geneSymbolFilterType">
            <form:options items="${geneSymbolValues}"/>
        </form:select>
        <form:errors path="zebrafishCriteriaBean.geneSymbolFilterType" cssClass="Error"/>
        <form:input path="zebrafishCriteriaBean.geneSearchTerm" size="6"/>
    </td>
    <td>
        <form:select path="zebrafishCriteriaBean.chromosomeFilterType">
            <form:options items="${chromosomeFilterValues}"/>
        </form:select>
        <form:input path="zebrafishCriteriaBean.chromosome" size="6"/>
    </td>
</tr>
<tr>
    <td colspan=4>
        <hr>
    </td>
</tr>
<tr>
    <td colspan=4>
        <b>Orthologs in:</b>
    </td>
</tr>
<tr>
    <td colspan=4>
        <form:radiobutton path="anyComparisonSpecies" value="true"/>ANY
    </td>
</tr>
<tr>
    <td>
        <form:radiobutton path="anyComparisonSpecies" value="false"/>Only:
    </td>
    <td>
        <b>Gene Symbol</b>:
    </td>
    <td>
        <b>Chromosome(s)</b>:
    </td>
    <td>
<%-- future implementation
        <b>Position</b>:
--%>
    </td>
</tr>
<tr>
    <td>
        <dd>
            <form:checkbox path="includeHuman"/> <%= Species.HUMAN.toString()%> (Homo sapiens)
        </dd>
    </td>
    <td>
        <form:select path="humanCriteriaBean.geneSymbolFilterType">
            <form:options items="${geneSymbolValues}"/>
        </form:select>
        <form:input path="humanCriteriaBean.geneSearchTerm" size="6"/>
    </td>
    <td>
        <form:select path="humanCriteriaBean.chromosomeFilterType">
            <form:options items="${chromosomeFilterValues}"/>
        </form:select>
        <form:input path="humanCriteriaBean.chromosome" size="6"/>
    </td>
    <td>
<%-- for future implementation
        <form:select path="humanCriteriaBean.positionFilterType">
            <option>begins</option>
            <option SELECTED>equals</option>
            <!-- <option>range</option> -->
        </form:select>
        <form:input path="humanCriteriaBean.position" size="6"/>
--%>
    </td>
</tr>

<!-- Mouse definitions -->
<tr>
    <td>
        <dd>
            <form:checkbox path="includeMouse"/> <%= Species.MOUSE.toString()%> (Mus musculus)
        </dd>
    </td>
    <td>
        <form:select path="mouseCriteriaBean.geneSymbolFilterType">
            <form:options items="${geneSymbolValues}"/>
        </form:select>
        <form:input path="mouseCriteriaBean.geneSearchTerm" size="6"/>
    </td>
    <td>
        <form:select path="mouseCriteriaBean.chromosomeFilterType">
            <form:options items="${chromosomeFilterValues}"/>
        </form:select>
        <form:input path="mouseCriteriaBean.chromosome" size="6"/>
    </td>
    <td>
<%-- for future implementation
        <form:select path="mouseCriteriaBean.positionFilterType">
            <option>begins</option>
            <option SELECTED>equals</option>
            <!-- <option>range</option> -->
        </form:select>
        <form:input path="mouseCriteriaBean.position" size="6"/>cM
--%>
    </td>
</tr>

<!-- Fly definitions -->
<tr>
    <td>
        <dd>
            <form:checkbox path="includeFly"/> <%= Species.FLY.toString()%> (Drosophila melanogaster)
        </dd>
    </td>
    <td>
        <form:select path="flyCriteriaBean.geneSymbolFilterType">
            <form:options items="${geneSymbolValues}"/>
        </form:select>
        <form:input path="flyCriteriaBean.geneSearchTerm" size="6"/>
    </td>
    <td>
        <form:select path="flyCriteriaBean.chromosomeFilterType">
            <form:options items="${chromosomeFilterValues}"/>
        </form:select>
        <form:input path="flyCriteriaBean.chromosome" size="6"/>
    </td>
    <td>
        &nbsp;
    </td>
</tr>

<tr>
    <td colspan=4>
        <HR>
        <table cellspacing="5">
            <tr>
                <td>
                    <B>Results per page:</B>
                    <form:input path="maxDisplayRecords" size="3" maxlength="4"/>
                </td>
                <td>
                    <b>Sort by:</b>
                </td>
                <td>
                    <form:radiobutton path="ordering" value="Zebrafish.symbol"/> Gene Symbol in Zebrafish
                </td>
            </tr>
            <tr>
                <td colspan="2"></td>
                <td>
                    <form:radiobutton path="ordering" value="Zebrafish.chromosome"/> Chromosomal location in Zebrafish
                </td>
            </tr>
        </table>
        <tr>
            <td colspan=4>
                <INPUT TYPE="submit" VALUE="Search"> <INPUT TYPE=reset>
            </td>
        </tr>
    </td>
</tr>
</table>
</td>
</tr>
</table>
</form:form>
