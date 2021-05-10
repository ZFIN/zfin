<#include "header-template.ftl">
<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">New terms added to ontology: ${root.rows?size} terms</td>
    </tr>
</table>
<hr/>
<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="300">Term Name</th>
        <th width="150">Term ID</th>
    </tr>
<#list root.rows as row>
    <#if row_index%2==0>
    <tr class="odd">
        <#else>
        <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row[1]}</td>
    <td>${row[0]}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">