<#include "header-template.ftl">
<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">${root.collection?size} relationships removed from ontology</td>
    </tr>
</table>
<hr/>
<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="300">Relationship ID</th>
        <th width="150">Parent Term ID</th>
        <th width="150">Parent Term Name</th>
        <th width="150">Child Term ID</th>
        <th width="150">Child Term Name</th>
        <th width="150">Relationship Type</th>
    </tr>
<#list root.collection as row>
    <#if row_index%2==0>
    <tr class="odd">
    <#else>
    <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row.zdbId}</td>
    <td>${row.termOne.oboID}</td>
    <td>${row.termOne.termName}</td>
    <td>${row.termTwo.oboID}</td>
    <td>${row.termTwo.termName}</td>
    <td>${row.type}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">