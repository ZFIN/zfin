<#include "header-template.ftl">
<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">New aliases added to ontology: ${root.rows?size} </td>
    </tr>
</table>
<hr/>
<table class="rowstripes">
    <tr>
        <th width="100">EXPATRES</th>
        <th width="100">Term Name</th>
        <th width="150">Obo ID</th>
        <th width="150">Start</th>
        <th width="150">End</th>
        <th width="150">Authors</th>
        <th width="150">PubID</th>
        <th width="150">Figure IDs</th>
    </tr>
<#list root.rows as row>
    <#if row_index%2==0>
    <tr class="odd">
        <#else>
        <tr class="even">
    </#if>
    <td>${row[0]}</td>
    <td>${row[1]}</td>
    <td>${row[2]}</td>
    <td>${row[3]}</td>
    <td>${row[4]}</td>
    <td>${row[5]}</td>
    <td>${row[6]}</td>
    <td>${row[7]}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">