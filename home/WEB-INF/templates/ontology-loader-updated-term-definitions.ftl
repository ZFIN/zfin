<#include "header-template.ftl">
<span class="section-title">Term Definitions updated in ontology</span>
<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="300">New Term Definition</th>
        <th width="300">Old Term Definition</th>
        <th width="150">Term ID</th>
    </tr>
<#list root.rows as row>
    <#if row_index%2==0>
    <tr class="odd">
        <#else>
        <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row[0]}</td>
    <td>${row[1]}</td>
    <td>${row[2]}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">